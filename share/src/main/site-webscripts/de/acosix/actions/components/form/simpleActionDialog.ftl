<#assign id=args.htmlid?html>
<#if formUI == "true">
   <@formLib.renderFormsRuntime formId=formId />
</#if>

<div id="${id}-dialog">
   <div id="${id}-dialogTitle" class="hd"></div>
   <div class="bd">

      <div id="${formId}-container" class="form-container">
   
         <#if form.showCaption?exists && form.showCaption>
            <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
         </#if>
      
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl}">

            <input type="hidden" id="${formId}-isPreviewSubmit" name="isPreviewSubmit" value="false" />

            <div id="${formId}-fields" class="form-fields">
               <#list form.structure as item>
                  <#if item.kind == "set">
                     <@formLib.renderSet set=item />
                  <#else>
                     <@formLib.renderField field=form.fields[item.id] />
                  </#if>
               </#list>
            </div>
            
            <div id="${id}-resultPreview" class="hidden">
            </div>

            <div class="bdft">
               <input id="${formId}-preview" type="button" class="hidden" value="${msg("acosix-actions.form.button.preview.label")}" />
               &nbsp;<input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />
               &nbsp;<input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
            </div>
      
         </form>

      </div>
   </div>
</div>