<@markup id="acosix-actions-css" target="css" action="after">
    <#include "/org/alfresco/components/form/form.css.ftl"/>
</@>
   
<@markup id="acosix-actions-js" target="js" action="after">
    <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="rules"/>
    <@script type="text/javascript" src="${url.context}/res/acosix/components/rules/config/rule-config-patches.js" group="rules"/>
    <@script type="text/javascript" src="${url.context}/res/acosix/components/rules/config/rule-config-action.js" group="rules"/>

    <#include "/org/alfresco/components/form/form.js.ftl"/>
</@>