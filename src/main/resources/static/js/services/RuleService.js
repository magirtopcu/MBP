/* global app */

/**
 * Provides services for managing rules.
 */
app.factory('RuleService', ['$http', '$resource', '$q', 'ENDPOINT_URI',
    function ($http, $resource, $q, ENDPOINT_URI) {
        //URLs for server requests
        const URL_GEt_RULE_ACTION_TYPES = ENDPOINT_URI + '/rules/action-types/';
        const URL_ENABLE_RULE = ENDPOINT_URI + '/rules/enable/';
        const URL_DISABLE_RULE = ENDPOINT_URI + '/rules/disable/';

        /**
         * [Public]
         * Performs a server request in order to retrieve all rule action types.
         *
         * @returns {*}
         */
        function getRuleActionTypes() {
            return $http.get(URL_GEt_RULE_ACTION_TYPES);
        }

        /**
         * [Public]
         * Performs a server request in order to enable a certain rule given by its id and returns the
         * resulting promise.
         *
         * @param ruleId The id of the rule to enable
         * @returns {*}
         */
        function enableRule(ruleId) {
            return $http.post(URL_ENABLE_RULE + ruleId);
        }

        /**
         * [Public]
         * Performs a server request in order to disable a certain rule given by its id and returns the
         * resulting promise.
         *
         * @param ruleId The id of the rule to disable
         * @returns {*}
         */
        function disableRule(ruleId) {
            return $http.post(URL_DISABLE_RULE + ruleId);
        }

        //Expose public methods
        return {
            getRuleActionTypes: getRuleActionTypes,
            enableRule: enableRule,
            disableRule: disableRule
        }
    }
]);

