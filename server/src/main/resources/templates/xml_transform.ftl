<#assign title><#if xt?has_content>XML-Transformation „${xt.name?html}“<#else>XML Transformations</#if></#assign>
<@ie.page title>
  <#if xt?has_content>
    <form method="post" enctype="multipart/form-data">
      <p><label for="xml">XML document:</label> <input type="file" name="xml" id="xml"></p>
      <p><input type="submit" value="Upload"></p>
    </form>
  </#if>
</@ie.page>