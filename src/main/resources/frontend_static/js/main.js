// This is a modified version of a minified file from https://na.leagueoflegends.com/en/featured/preseason-update#builder
webpackJsonp(
	[2],
	{
		11: function (t, e, n) {
			"use strict";

			function r(t) {
				return "" + versionedAssetPath + t;
			}

			function o(t) {
			}

			Object.defineProperty(e, "__esModule", {value: !0});
			var a =
				Object.assign ||
				function (t) {
					for (var e = 1; e < arguments.length; e++) {
						var n = arguments[e];
						for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && (t[r] = n[r]);
					}
					return t;
				};
			(e.assetPath = r), (e.pushToDataLayer = o);
			e.default = {assetPath: r, pushToDataLayer: o};
		},
		241: function (t, e, n) {
			"use strict";
			n(242), n(443), n(444), n(445), n(446), n(447), n(448);
		},
		443: function (t, e, n) {
			"use strict";

			function r() {
				(i.style.transform = "translate3d(0, " + Math.floor(0.35 * a.default.viewportTop) + "px, 0)"), (c = window.requestAnimationFrame(r));
			}

			var o = n(58),
				a = (function (t) {
					return t && t.__esModule ? t : {default: t};
				})(o),
				i = document.querySelector(".js-hero-backdrop"),
				c = null;
			if (i) {
				var u = a.default.create(i);
				u &&
				(u.enterViewport(r),
					u.exitViewport(function () {
						window.cancelAnimationFrame(c);
					}));
			}
		},
		444: function (t, e, n) {
			"use strict";

			function r() {
				(c.style.paddingTop = u.offsetHeight + "px"), u.classList.add("is-fixed");
			}

			function o() {
				(c.style.paddingTop = ""), u.classList.remove("is-fixed");
			}

			var a = n(58),
				i = (function (t) {
					return t && t.__esModule ? t : {default: t};
				})(a),
				u = null;
		},
		445: function (t, e, n) {
			"use strict";
			n(188).polyfill(),
				[]
					.concat(
						(function (t) {
							if (Array.isArray(t)) {
								for (var e = 0, n = Array(t.length); e < t.length; e++) n[e] = t[e];
								return n;
							}
							return Array.from(t);
						})(document.querySelectorAll(".js-scroll-to"))
					)
					.forEach(function (t) {
						t.addEventListener("click", function () {
							var e = document.querySelector(t.hash);
							e && window.scrollTo({
								top: e.getBoundingClientRect().top + (window.scrollY || window.pageYOffset),
								left: 0,
								behavior: "smooth"
							});
						});
					});
		},
		446: function (t, e, n) {
			"use strict";
			var r = n(58),
				o = (function (t) {
					return t && t.__esModule ? t : {default: t};
				})(r);
			[]
				.concat(
					(function (t) {
						if (Array.isArray(t)) {
							for (var e = 0, n = Array(t.length); e < t.length; e++) n[e] = t[e];
							return n;
						}
						return Array.from(t);
					})(document.querySelectorAll(".js-active-in-view"))
				)
				.forEach(function (t) {
					var e = o.default.create(t, 0.1 * window.innerHeight);
					e.enterViewport(function () {
						t.classList.add("is-active"), e.destroy();
					});
				});
		},
		447: function (t, e, n) {
			"use strict";

			function r(t) {
				if (Array.isArray(t)) {
					for (var e = 0, n = Array(t.length); e < t.length; e++) n[e] = t[e];
					return n;
				}
				return Array.from(t);
			}

			function o(t) {
				return new Promise(function (e, n) {
					var r = new Image(),
						o = t.getAttribute("data-src");
					o || n("No 'data-src' set on " + t),
						(r.onload = function () {
							(t.src = o), e();
						}),
						(r.onerror = function () {
							n();
						}),
						(r.src = o);
				});
			}

			function a() {
				s.forEach(function (t) {
					t.style.background = "";
				});
			}

			var i = n(58),
				c = (function (t) {
					return t && t.__esModule ? t : {default: t};
				})(i),
				u = n(11),
				l = [].concat(r(document.querySelectorAll(".js-lazy-image"))),
				s = [].concat(r(document.querySelectorAll(".js-lazy-background")));
			l.forEach(function (t) {
				var e = c.default.create(t, 0.5 * window.innerHeight),
					n = window.getComputedStyle(t),
					r = "all 0s ease 0s" === n.transition;
				(t.style.opacity = 0),
					(t.src = (0, u.assetPath)("/img/global/blank.gif")),
				r && (t.style.transition = "opacity 300ms"),
					e.enterViewport(function () {
						o(t).then(function () {
							(t.style.opacity = ""),
								window.setTimeout(function () {
									r && (t.style.transition = "");
								}, 300);
						});
					});
			}),
				window.addEventListener("load", a);
		},
		448: function (t, e, n) {
			"use strict";

			function r(t) {
				if (Array.isArray(t)) {
					for (var e = 0, n = Array(t.length); e < t.length; e++) n[e] = t[e];
					return n;
				}
				return Array.from(t);
			}

			var o = n(58),
				a = (function (t) {
					return t && t.__esModule ? t : {default: t};
				})(o),
				i = n(11),
				c = [].concat(r(document.querySelectorAll(".js-scroll-marker")));
			c &&
			c.forEach(function (t) {
				var e = a.default.create(t),
					n = {eventAction: "Scroll", eventLabel: t.id};
				e.fullyEnterViewport(function () {
					(0, i.pushToDataLayer)(n);
				});
			});
			var u = [].concat(r(document.querySelectorAll(".js-push-click")));
			u &&
			u.forEach(function (t) {
				var e = t.getAttribute("data-event-label"),
					n = {eventAction: "Click", eventLabel: e};
				t.addEventListener("click", function () {
					(0, i.pushToDataLayer)(n);
				});
			});
		}
	},
	[241]
);
