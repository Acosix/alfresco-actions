<#escape x as jsonUtils.encodeJSONString(x)>{
    "signalName":"${signalName!"mysignal"}",
    "tenantId":"${tenantId!"tenant_1"}",
    "async":"${async!"false"}",
    "variables":
    [
        {
            "name":"document",
            "value":"${document.nodeRef}"
        }
    ]
}</#escape>