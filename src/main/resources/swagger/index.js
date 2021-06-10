window.onload = function() {
  const extract = function(v) {
    return decodeURIComponent(
        v.replace(
          /(?:(?:^|.*;\s*)contextPath\s*\=\s*([^;]*).*$)|^.*$/,
          '$1'
        )
      );
  };

  const contextPath = extract(document.cookie);

  const ui = SwaggerUIBundle({
    url: `${contextPath}/swagger`,
    dom_id: '#swagger-ui',
    tagsSorter: 'alpha',
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl],
    validatorUrl: null,
    deepLinking: true
  });

  window.ui = ui;
};
