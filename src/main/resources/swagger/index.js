window.onload = function() {
  let contextPath = window.location.pathname;
  contextPath = contextPath.endsWith('/') ? contextPath.slice(0, -1) : contextPath;

  const ui = SwaggerUIBundle({
    url: `./swagger`,
    dom_id: '#swagger-ui',
    tagsSorter: 'alpha',
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl, updateContextPath(contextPath)],
    validatorUrl: null,
    deepLinking: true
  });

  window.ui = ui;
};

function updateContextPath(contextPath) {
  return {
    statePlugins: {
      spec: {
        wrapActions: {
          updateJsonSpec: (oriAction) => (...args) => {
            const [spec] = args;
            if (spec && spec.paths) {
              const newPaths = {};
              Object.entries(spec.paths).forEach(
                ([path, value]) => (newPaths[contextPath + path] = value)
              );
              spec.paths = newPaths;
            }
            oriAction(...args);
          },
        },
      },
    },
  };
}
