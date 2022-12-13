<%
    ui.includeJavascript("cdrsync", "jquery.js")
    ui.includeCss("cdrsync", "style.css")

    def id = config.id
%>
<%= ui.resourceLinks() %>

<div class="flex_container">
    <div>
        <button id="initial" style="color: red"><b>Sync From Initial</b></button>
    </div>
    <div>
        <button id="sync" style="color: green"><b>Sync Update</b></button>
    </div>
    <div>
        <button id="custom" style="color: blue"><b>Custom Sync</b></button>
    </div>
</div>
<br/>
<b/>
<div class="input_container" id="custom_date">
    <div>
        <input type="date" id="start" name="startDate"/>
    </div>

    <div>
        <input type="date" id="end" name="endDate"/>
    </div>
    <br/>
    <div>
        <button id="custom_sync" style="color: blue">Sync</button>
    </div>
</div>

<script type="text/javascript">
    var jq = jQuery;

    jq(document).ready(function (){
        jq("#custom_date").hide();
        jq("#initial").click(function(){
            console.log("I am clicked");
            alert("I am clicked");
            jq.ajax({
                url: "${ui.actionLink("getPatients")}",
                dataType: "json"
            }).success(function (){
                alert("I am clicked");
            })
        });

        jq("#sync").click(function(){
            console.log("I am clicked");
            alert("I am clicked");
            jq.ajax({
                url: "${ui.actionLink("getPatientsFromLastSync")}",
                dataType: "json"
            }).success(function (){
                alert("I am clicked");
            })
        });
        jq("#custom").click(function (){
            jq("#custom_date").show();
            jq("#custom_sync").click(function (){
                var startDate = jq("#start").val();
                var endDate = jq("#end").val();
                if (startDate === "") {
                    alert("Please choose a start date");
                } else if (endDate === "") {
                    alert("Please choose an end date");
                } else {
                    alert(startDate +"   " + endDate);
                }
            })
        })

    });


</script>