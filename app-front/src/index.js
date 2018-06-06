
// Polyfill for CustomEvent() constructor functionality in Internet Explorer 9 and higher.
(function () {
  if (typeof window.CustomEvent === "function") return false; //If not IE

  function CustomEvent(event, params) {
    params = params || { bubbles: false, cancelable: false, detail: undefined };
    var evt = document.createEvent('CustomEvent');
    evt.initCustomEvent(event, params.bubbles, params.cancelable, params.detail);
    return evt;
  }

  CustomEvent.prototype = window.Event.prototype;

  window.CustomEvent = CustomEvent;
})();


window.onload = function () {
  var ua = window.navigator.userAgent;
  var version;
  var edge;
  var msie = ua.indexOf('MSIE ');
  if (msie > 0) {
    // IE 10 or older
    version = parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
  } else {
    var trident = ua.indexOf('Trident/');
    if (trident) {
      // IE 11
      var rv = ua.indexOf('rv:');
      version = parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
    }
    edge = ua.indexOf('Edge/');
    if (edge) {
      // Edge (IE 12+)
      version = parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
    }
  }

  if (version && version < 11) {
    document.body.innerHTML = '<p>申し訳ありませんがお使いのブラウザはサポートされていません。<br>Chrome、FireFox、Edge もしくは IE11をご利用ください。</p><br>' +
      '<p>Sorry, your browser is not supported.<br>Please use Chrome, FireFox, Edge or IE11.</p>';
  } else {
    if (!version || edge) {
      const LARGE_WIDTH = 1600;
      const MEDIUM_WIDTH = 1280;
      const SMALL_WIDTH = 1152;
      const MINIMUM_WIDTH = 800;
      var sw = window.screen.width;
      if (sw > LARGE_WIDTH) {
        document.body.classList.add('wide');
      } else if (sw > MEDIUM_WIDTH) {
        document.body.classList.add('large');
      } else if (sw > SMALL_WIDTH) {
        document.body.classList.add('medium');
      } else if (sw > MINIMUM_WIDTH) {
        document.body.classList.add('small');
      } else {
        document.body.classList.add('mini');
      }
    }

    document.dispatchEvent(new CustomEvent('startNgLoad'));
  }


}
