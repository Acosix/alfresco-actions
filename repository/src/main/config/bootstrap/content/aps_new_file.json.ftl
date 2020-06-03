<#escape x as jsonUtils.encodeJSONString(x)>{
    "signalName":"mysignal",
    "tenantId":"tenant_1",
    "async":"false",
    "variables":
    [
        {
            "name":"document",
            "value":"${document.nodeRef}"
        }
    ]
}</#escape>