window.onload = function() {
  let contextPath = window.location.pathname;
  contextPath = contextPath.endsWith('/') ? contextPath.slice(0, -1) : contextPath;

  const ui = SwaggerUIBundle({
    defaultModelsExpandDepth: 0,
    deepLinking: true,
    // docExpansion: 'full',
    dom_id: '#swagger-ui',
    layout: 'BaseLayout',
    plugins: [SwaggerUIBundle.plugins.DownloadUrl, updateContextPath(contextPath)],
    presets: [SwaggerUIBundle.presets.apis],
    tagsSorter: 'alpha',
    tryItOutEnabled: true,
    validatorUrl: null,
    url: `./swagger`,
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
