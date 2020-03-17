window.onload = function() {
  const extract = function(v) {
    return decodeURIComponent(
        v.replace(
          /(?:(?:^|.*;\s*)contextPath\s*\=\s*([^;]*).*$)|^.*$/,
          "$1"
        )
      );
    },
    cookie = extract(document.cookie),
    contextPath = cookie === "" ? extract(window.location.search.substring(1)) : cookie,
    f = contextPath === "" ? undefined : function(system) {
      return {
        statePlugins: {
          spec: {
            wrapActions: {
              updateJsonSpec: (oriAction, system) => (...args) => {
                let [spec] = args;
                if (spec && spec.paths) {
                  const newPaths = {};
                  Object.entries(spec.paths)
                      .forEach(([path, value]) => (newPaths[contextPath + path] = value));
                  spec.paths = newPaths;
                }
                oriAction(...args);
              }
            }
          }
        }
      };
    },
    ui = SwaggerUIBundle({
      url: contextPath + "/swagger/haz-services.yml",
      dom_id: "#swagger-ui",
      tagsSorter: 'alpha',
      presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
      plugins: [SwaggerUIBundle.plugins.DownloadUrl, f],
      validatorUrl: null,
      deepLinking: true
    });
  window.ui = ui;
};
