'use strict';

import Tools from './Tools.js';
import NshmpError from '../error/NshmpError.js';

/**
* @fileoverview Static method to read two possible config files:
*     1. /nshmp-haz-v2/apps/js/lib/config.json
*     2. /nshmp-haz-v2/config.json
* The first config file, /nshmp-haz-v2/apps/config.json, is
*     the default config file and remains in the repository.
* The second config file, /nshmp-haz-v2/config.js, is ignored by github and
*     is for developmental purposes. If this file exists it will be read in 
*     and merged with the default, overriding any keys present in the first
*     config file.
* The second file will be ignored if it is not present.
*
* @class Config
* @author bclayton@usgs.gov (Brandon Clayton)
*/
export default class Config {

  /**
  * @param {Class} callback - The callback must be a class as 
  *     new callback(config) will be called. 
  */
  static getConfig(callback) {
    const base = 'nshmp-haz-v2';

    let mainConfigUrl = `/${base}/apps/config.json`;
    let overrideConfigUrl = `/${base}/config.json`;
    
    let jsonCall = Tools.getJSONs([mainConfigUrl, overrideConfigUrl]);

    Promise.all(jsonCall.promises).then((responses) => {
      let mainConfig = responses[0];
      let overrideConfig = responses[1];

      let config = $.extend({}, mainConfig, overrideConfig);
      config.base = base;
      new callback(config);
    }).catch((errorMessage) => {
      if (errorMessage != 'Could not reach: /nshmp-haz-v2/config.json') return;
      console.clear();
      jsonCall.promises[0].then((config) => {
        config.base = base;
        new callback(config);
      }).catch((errorMessage) => {
        if (errorMessage != `Could not reach: /${base}/apps/config.json`) return;
        NshmpError.throwError(errorMessage);
      });
    });
  }

}
