/** Injected into Instagram mobile web to count scroll gestures. */
export const SCROLL_BRIDGE_JS = `
(function () {
  if (window.__instaScrollBridge) return true;
  window.__instaScrollBridge = true;

  var UNIT = 280;
  var TOUCH_MIN = 40;
  var pending = 0;
  var lastTouchY = null;
  var gestureArmed = true;
  var lastGestureAt = 0;

  function post(scrolls, distance) {
    try {
      window.ReactNativeWebView.postMessage(JSON.stringify({
        type: 'scroll',
        scrolls: scrolls,
        distance: distance
      }));
    } catch (e) {}
  }

  function flushDistance() {
    if (pending < UNIT) return;
    var units = Math.floor(pending / UNIT);
    var used = units * UNIT;
    pending -= used;
    post(units, used);
  }

  function markGesture() {
    var now = Date.now();
    if (!gestureArmed && now - lastGestureAt < 220) return;
    gestureArmed = false;
    lastGestureAt = now;
    post(1, 0);
    setTimeout(function () { gestureArmed = true; }, 220);
  }

  document.addEventListener('touchstart', function (e) {
    if (!e.touches || !e.touches[0]) return;
    lastTouchY = e.touches[0].clientY;
  }, { passive: true, capture: true });

  document.addEventListener('touchmove', function (e) {
    if (lastTouchY == null || !e.touches || !e.touches[0]) return;
    var y = e.touches[0].clientY;
    var delta = Math.abs(y - lastTouchY);
    lastTouchY = y;
    if (delta < 1) return;
    pending += delta;
    if (pending >= TOUCH_MIN) markGesture();
    flushDistance();
  }, { passive: true, capture: true });

  document.addEventListener('touchend', function () {
    lastTouchY = null;
  }, { passive: true, capture: true });

  window.addEventListener('scroll', function () {
    markGesture();
  }, { passive: true, capture: true });

  window.addEventListener('wheel', function () {
    markGesture();
  }, { passive: true, capture: true });

  true;
})();
`;

export const MOBILE_USER_AGENT =
  "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1";
