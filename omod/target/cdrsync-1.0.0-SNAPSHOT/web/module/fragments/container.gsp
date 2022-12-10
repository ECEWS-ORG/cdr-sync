<%
    ui.includeJavascript("cdrsync", "jquery.js")

    def id = config.id
%>
<%= ui.resourceLinks() %>

<div style="color: blue; align-self: center">
    <button id="sync" style="color: red; align-self: center">Sync Data</button>
</div>

<script type="text/javascript">
    var jq = jQuery;

    jq(document).ready(function (){
        jq("#sync").click(function(){
            console.log("I am clicked");
            alert("I am clicked");
            jq.ajax({
                url: "${ui.actionLink("getPatients")}",
                dataType: "json"
            }).success(function (){
                alert("I am clicked");
            })
        });


    });


</script>