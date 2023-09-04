<%
    ui.includeCss("cdrsync", "style.css")
    ui.includeCss("cdrsync", "cardPage.css")

%>
<%= ui.resourceLinks() %>
<script src="https://rawgit.com/schmich/instascan-builds/master/instascan.min.js"></script>
<section class="home-content-top">
    <div class="container">

        <!--our-quality-shadow-->
        <div class="clearfix"></div>
        <h1 class="heading1">Welcome to CDR-SYNC Module</h1>
        <div class="tabbable-panel margin-tops4 ">
            <div class="tabbable-line">
                <ul class="nav nav-tabs tabtop  tabsetting">
                    <li class="active"> <a href="#tab_default_1" data-toggle="tab"> Card Scanner </a> </li>
                    <li> <a href="#tab_default_2" data-toggle="tab"> CDR Extraction </a> </li>
                </ul>
                <div class="tab-content margin-tops">
                    <div class="tab-pane active fade in" id="tab_default_1">
                        <div class="col-md-8 card-button-container">
                            <div style="width:50%">
                                <button id="card_scanner" style="color: red"><b>Scan Card</b></button>
                                <div id="pepfar_id" style="width:50%">
                                    <input type="text" id="pepfar">
                                </div>
                            </div>
                            <div style="width:50%">
                                <button id="qr_scanner" style="color: red"><b>Scan QR</b></button>
                                <video id="video" style="width: 100%;margin-top: 10px;"></video>
                            </div>

                        </div>
                            <table id="patients">
                                <thead>
                                <tr>
                                    <th>Patient Name</th>
                                    <th>Phone Number</th>
                                    <th>Patient Identifier</th>
                                    <th>Identifier Type</th>
                                    <th>Voided</th>
                                    <th>Action</th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                    </div>
                    <div class="tab-pane fade" id="tab_default_2">
                        <div id="overlay">
                            <div class="cv-spinner">
                                <span class="spinner"></span>
                            </div>
                        </div>
                        <div class="container-wrap">
                            <h3>Last Sync Date: <%= lastSyncDate != null ? lastSyncDate : "N/A" %></h3>
                            <div id="message"></div>
                            <div class="flex-container">
                                <div>
                                    <button id="initial" style="color: red"><b>Sync From Initial</b></button>
                                </div>
                                <div>
                                    <button id="update" style="color: green"><b>Sync Update</b></button>
                                </div>
                                <div>
                                    <button id="custom" style="color: blue"><b>Custom Sync</b></button>
                                </div>
                            </div>
                            <br/>
                            <br/>
                            <div class="input-container" id="custom_date">
                                <div>
                                    <label for="start"><b>Start Date</b></label>
                                    <br/>
                                    <input type="date" id="start" name="startDate"/>
                                </div>

                                <div>
                                    <label for="end"><b>End Date</b></label>
                                    <br/>
                                    <input type="date" id="end" name="endDate"/>
                                </div>
                                <br/>
                                <div>
                                    <button id="custom_sync" style="color: blue"><em><b>Sync</b></em></button>
                                </div>
                            </div>
                        </div>
                        <br/>
                        <br/>
                        <table>
                            <thead>
                            <tr>
                                <th>Created by</th>
                                <th>Total patients processed</th>
                                <th>Total patients</th>
                                <th>Sync type</th>
                                <th>Sync status</th>
                                <th>Date started</th>
                                <th>Date completed</th>
                                <th>Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                if (recentSyncBatches != null) {
                                    for (int i = 0; i < recentSyncBatches.size(); i++) {
                            %>
                            <tr>
                                <td><%= recentSyncBatches.get(i).getOwnerUsername() %></td>
                                <td><%= recentSyncBatches.get(i).getPatientsProcessed() %></td>
                                <td><%= recentSyncBatches.get(i).getPatients() %></td>
                                <td><%= recentSyncBatches.get(i).getSyncType() %></td>
                                <td><%= recentSyncBatches.get(i).getStatus() %></td>
                                <td><%= recentSyncBatches.get(i).getDateStarted() %></td>
                                <td><%= recentSyncBatches.get(i).getDateCompleted() %></td>
                                <td>
                                    <i style="font-size: 20px;" class="icon-play edit-action" title="resume"></i>
                                    <i style="font-size: 20px;" class="icon-remove edit-action" title="delete file"></i>
                                    <i style="font-size: 20px;" class="icon-refresh edit-action" title="rerun file"></i>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>


<script type="text/javascript">
    var jq = jQuery;
    jq("#custom_date").hide();
    jq("#pepfar_id").hide();
    jq("#patients").hide();
    jq("#video").hide();

    jq("#card_scanner").click(function () {
        // let nfcCardId = "Q6yUygkQmb";
        var nfcCardId = makeId(10);
        console.log(nfcCardId);
        getNfcCardMapperByNfcCardId(nfcCardId).then(resp => {
            var nfcCardMapperByNfcCardId = resp.body;
            console.log(nfcCardMapperByNfcCardId);
            if (nfcCardMapperByNfcCardId["data"] !== null) {
                alert("Card already mapped to a patient");
                var url = nfcCardMapperByNfcCardId["data"];
                redirectToPatientDashboard(url);
            } else {
                jq("#pepfar_id").show();
                jq("#video").hide();

                jq("#pepfar").keyup(function () {
                    var pepfarId = jq.trim(this.value);
                    if (pepfarId !== "") {
                        getPatientDetails(pepfarId).then(resp => {
                            var patient = resp.body;
                            if (patient["data"] !== null) {
                                jq("#patients").show();
                                jq("#patients tbody").empty();
                                var patientDetails = patient["data"];
                                console.log(patientDetails);
                                if (patientDetails.length <= 30) {
                                    jq.each(patientDetails, function (key, value) {
                                        jq("#patients tbody").append("<tr>" +
                                            "<td>" + value.patientName + "</td>" +
                                            "<td>" + value.patientPhoneNumber + "</td>" +
                                            "<td>" + value.patientIdentifier + "</td>" +
                                            "<td>" + value.identifierType + "</td>" +
                                            "<td>" + value.voided + "</td>" +
                                            "<td><button onclick=mapCardToPatient('" + nfcCardId + "','" +
                                                value.patientIdentifier + "','" + value.patientPhoneNumber + "','" +
                                                value.patientUuid + "')>Map</button></td>" +
                                            "</tr>");
                                    });
                                }

                            } else {
                                alert("Patient not found");
                            }
                        }, error => {
                            console.log(error);
                            alert(error.statusText);
                        });
                    }
                })



                // jq("#pepfar_submit").click(function () {
                //     var pepfarId = jq("#pepfar").val();
                //     if (pepfarId === "") {
                //         alert("Please enter a pepfar id");
                //     } else {
                //         getNfcCardMapperByPepfarId(pepfarId).then(resp => {
                //             var nfcCardMapperByPepfarId = resp.body;
                //             console.log(nfcCardMapperByPepfarId);
                //             if (nfcCardMapperByPepfarId["data"] !== null) {
                //                 alert("Pepfar id already mapped to a card");
                //             } else {
                //                 getPatientDetails(pepfarId).then(resp => {
                //                     var patient = resp.body;
                //                     console.log(patient);
                //                     if (patient["data"] !== null) {
                //                         var patientDetails = patient["data"];
                //                         console.log(patientDetails);
                //                         var patientPhoneNo = patientDetails.patientPhoneNumber;
                //                         console.log(patientPhoneNo);
                //                         var patientUuid = patientDetails.patientUuid;
                //                         console.log(patientUuid);
                //                         var patientName = patientDetails.patientName
                //                         saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid, patientName).then(resp => {
                //                             var response = resp.body;
                //                             console.log(response);
                //                             if (response["message"] === "Successful") {
                //                                 alert("Card "+nfcCardId+" successfully mapped to " + pepfarId);
                //                                 redirectToPatientDashboard(response["data"]);
                //                             } else {
                //                                 alert("Failed to map card to " + pepfarId);
                //                             }
                //                         }, error => {
                //                             console.log(error);
                //                             alert(error.statusText);
                //                         });
                //                     } else {
                //                         alert("Patient not found");
                //                     }
                //                 }, error => {
                //                     console.log(error);
                //                     alert(error.statusText);
                //                 });
                //             }
                //         }, error => {
                //             console.log(error);
                //             alert(error.statusText);
                //         });
                //     }
                // });
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    });

    function mapCardToPatient(nfcCardId, pepfarId, patientPhoneNo, patientUuid) {
        getNfcCardMapperByPepfarId(pepfarId).then(resp => {
            var nfcCardMapperByPepfarId = resp.body;
            console.log(nfcCardMapperByPepfarId);
            if (nfcCardMapperByPepfarId["data"] !== null) {
                alert("Patient already mapped to a card");
            } else {
                saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid).then(resp => {
                    var response = resp.body;
                    console.log(response);
                    if (response["message"] === "Successful") {
                        alert("Card "+nfcCardId+" successfully mapped to " + pepfarId);
                        redirectToPatientDashboard(response["data"]);
                    } else {
                        alert("Failed to map card to " + pepfarId);
                    }
                }, error => {
                    console.log(error);
                    alert(error.statusText);
                });
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    }

    jq("#qr_scanner").click(function () {
        jq("#pepfar_id").hide();
        jq("#video").show();
        jq("#patients").hide();
        navigator.mediaDevices.getUserMedia({ video: true })
            .then(stream => {
                const videoElement = document.getElementById('video');
                videoElement.srcObject = stream;

                // Initialize the QR code scanner
                const scanner = new Instascan.Scanner({ video: videoElement });
                scanner.addListener('scan', content => {
                    console.log('QR Code detected:', content);
                    // You can perform any action you want with the scanned content
                    // For example, redirecting to a URL
                    redirectToPatientDashboard(content);
                });

                // Start scanning
                Instascan.Camera.getCameras()
                    .then(cameras => {
                        if (cameras.length > 0) {
                            scanner.start(cameras[0]); // Start scanning using the first available camera
                        } else {
                            console.error('No cameras found.');
                        }
                    })
                    .catch(error => {
                        console.error('Error accessing cameras:', error);
                    });
            })
            .catch(error => {
                console.error('Error accessing webcam:', error);
            });
    })


    jq("#patientDetails").click(function (){
        var patientIdentifier = prompt("Please enter patient identifier", "");
        if (patientIdentifier != null) {
            getPatientDetails(patientIdentifier).then(resp => {
                var patient = resp.body;
                console.log(patient)
                if (patient !== "") {
                    alert("Patient details: " + patient);
                } else {
                    alert("Patient not found");
                }
            }, error => {
                console.log(error);
                alert(error.statusText);
            });
        }
    });

    jq("#initial").click(function(){
        alert("Syncing from inception");
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        patientCountFromInitial().then(resp => {
            const totalPatients = resp.body;
            console.log("Total patients to sync: " + totalPatients);
            if (totalPatients > 0) {
                getPatientsProcessed(totalPatients, "INITIAL").then(resp => {
                    const response = resp.body.split("/");
                    const start = parseInt(response[0]);
                    let id = parseInt(response[1]);
                    const length = totalPatients - start < 500 ? totalPatients - start : 500;
                    alert("Syncing from " + start + " to " + totalPatients);
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        getPatientsProcessed(totalPatients, "INITIAL").then(resp => {
                            id = parseInt(resp.body.split("/")[1]);
                            batchSyncFromInitial(totalPatients, start, length, id);
                        });
                    } else {
                        batchSyncFromInitial(totalPatients, start, length, id);
                    }
                })

            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    });

    function batchSyncFromInitial(total, start, length, id) {
        let serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            const percentage = Math.round(((start + length) / total) * 100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + total +" patients</p>");
        }
        syncInitial(total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1 &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INITIAL", id, total);
                batchSyncFromInitial(total, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                var response = serverResponse.split(",");
                serverResponse = response[0];
                var zipFiles = response[1].split("&&");
                console.log(zipFiles);
                jq('#message').html("<p>"+serverResponse+"</p>" +
                    "<p>Click the download button(s) below to download the extracted files</p><br>"
                );
                for (var i = 0; i < zipFiles.length; i++) {
                    if (zipFiles[i] !== "") {
                        jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download</button>");
                    }
                }
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }

        }, error => {
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    jq("#update").click(function(){
        alert("Syncing from last sync date");
        window.onbeforeunload = function() {
            return "Dude, are you sure you want to leave? Think of the kittens!";
        };
        patientCountFromLastSync().then(resp => {
            var count = resp.body;
            console.log("Total patients to sync: " + count);
            if (count > 0) {
                getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                    var response = resp.body.split("/");
                    var start = parseInt(response[0]);
                    var id = parseInt(response[1]);
                    var length = (count-start) < 500 ? count-start : 500;
                    alert("Syncing from " + start + " to " + count);
                    jq('#overlay').fadeIn(300);
                    if (id === 0) {
                        getPatientsProcessed(count, "INCREMENTAL").then(resp => {
                            id = parseInt(resp.body.split("/")[1]);
                            batchSyncFromLastSync(count, start, length, id)
                        });
                    } else {
                        batchSyncFromLastSync(count, start, length, id);
                    }
                })
            } else {
                alert("No new patients to sync");
            }
        }, error => {
            console.log(error);
            alert(error.statusText);
        });
    });

    function batchSyncFromLastSync(totalPatients, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= totalPatients) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            var percentage = Math.round(((start+length)/totalPatients)*100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + totalPatients +" patients</p>");
        }
        syncUpdate(totalPatients, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1  &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = totalPatients - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "INCREMENTAL", id, totalPatients);
                batchSyncFromLastSync(totalPatients, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                var response = serverResponse.split(",");
                serverResponse = response[0];
                var zipFiles = response[1].split("&&");
                console.log(zipFiles);
                jq('#message').html("<p>"+serverResponse+"</p>" +
                    "<p>Click the download button(s) below to download the extracted files</p><br>"
                );
                for (var i = 0; i < zipFiles.length; i++) {
                    if (zipFiles[i] !== "") {
                        jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download</button>");
                    }
                }
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
        }, error => {
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

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
                alert("Syncing patients from " + startDate + " to " + endDate);
                window.onbeforeunload = function() {
                    return "Dude, are you sure you want to leave? Think of the kittens!";
                };
                patientCountFromCustomDate(startDate, endDate).then(resp => {
                    var count = resp.body;
                    console.log("Total patients to sync: " + count);
                    if (count > 0) {
                        getPatientsProcessed(count, "CUSTOM").then(resp => {
                            var response = resp.body.split("/");
                            var start = parseInt(response[0]);
                            var id = parseInt(response[1]);
                            var length = (count-start) < 500 ? count-start : 500;
                            alert("Syncing from " + start + " to " + count);
                            jq('#overlay').fadeIn(300);
                            if (id === 0) {
                                getPatientsProcessed(count, "CUSTOM").then(resp => {
                                    id = parseInt(resp.body.split("/")[1]);
                                    alert("id: " + id);
                                    batchSyncFromCustomDate(startDate, endDate, count, start, length, id);
                                });
                            } else {
                                alert("id: " + id);
                                batchSyncFromCustomDate(startDate, endDate, count, start, length, id);
                            }

                        })
                    } else {
                        alert("No new patients to sync");
                    }
                }, error => {
                    console.log(error);
                    alert(error.statusText);
                });
            }
        });
    });

    function batchSyncFromCustomDate(from, to, total, start, length, id) {
        var serverResponse = "";
        console.log("Syncing from " + start + " to " + (start + length));

        if (start >= total) {
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>Currently zipping extracted files</p>");
        } else {
            var percentage = Math.round(((start+length)/total)*100);
            jq('#message').html("<p>Extracting data for CDR, please don't refresh the page</p>" +
                "<p>"+ percentage + "% of " + total +" patients</p>");
        }
        syncCustom(from, to, total, start, length, id).then(resp => {
            serverResponse = resp.body;
            if (serverResponse.indexOf("Sync complete!") === -1 &&
                serverResponse !== "There's a problem connecting to the server. Please, check your connection and try again." &&
                serverResponse !== "Incomplete syncing, try again later!")
            {
                start = start + length;
                var remaining = total - start;
                if (remaining < length) {
                    length = remaining;
                }
                updateCdrSyncBatch(start, "CUSTOM", id, total);
                batchSyncFromCustomDate(from, to, total, start, length, id);
            } else if (serverResponse.indexOf("Sync complete!") >= 0) {
                var response = serverResponse.split(",");
                serverResponse = response[0];
                var zipFiles = response[1].split("&&");
                console.log(zipFiles);
                jq('#message').html("<p>"+serverResponse+"</p>" +
                    "<p>Click the download button(s) below to download the extracted files</p><br>"
                );
                for (var i = 0; i < zipFiles.length; i++) {
                    if (zipFiles[i] !== "") {
                        jq('#message').append("<button onclick=downloadFile('" + zipFiles[i] +"') >Download file</button>");
                    }
                }
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
            } else {
                setTimeout(function(){
                    jq("#overlay").fadeOut(300);
                },500);
                jq('#message').html("<p>"+serverResponse+"</p>");
            }
        }, error => {
            setTimeout(function(){
                jq("#overlay").fadeOut(300);
            },500);
            console.log(error);
            alert(error.statusText);
        });
    }

    function downloadFile(fileName) {
        window.location = fileName;
    }

    function redirectToPatientDashboard(url) {
        window.location.href = url;
    }

    function syncUpdate(total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromLastSync")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total,
                'id': id
            }
        }));
    }

    function syncInitial(total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromInitial")}",
            dataType: "json",
            data: {
                'start': start,
                'length': length,
                'total': total,
                'id': id
            }
        }))
    }

    function patientCountFromInitial() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCount")}",
            dataType: "json"
        }))
    }

    function getPatientsProcessed(total, type) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsProcessed")}",
            dataType: "json",
            data: {
                'total': total,
                'type': type
            }
        }))
    }

    function patientCountFromLastSync() {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCountFromLastSync")}",
            dataType: "json"
        }))
    }

    function patientCountFromCustomDate(from, to) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsCountFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to
            }
        }))
    }

    function getNfcCardMapperByNfcCardId(nfcCardId) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getNfcCardMapperByNfcCardId")}",
            dataType: "json",
            data: {
                'nfcCardId': nfcCardId
            }
        }))
    }

    function getNfcCardMapperByPepfarId(pepfarId) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getNfcCardByPatientIdentifier")}",
            dataType: "json",
            data: {
                'patientIdentifier': pepfarId
            }
        }))
    }

    function saveNfcCardMapper(nfcCardId, pepfarId, patientPhoneNo, patientUuid) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "saveNfcCardMapper")}",
            dataType: "json",
            data: {
                'nfcCardId': nfcCardId,
                'patientIdentifier': pepfarId,
                'patientPhoneNo': patientPhoneNo,
                'patientUuid': patientUuid
            },
            beforeSend: function () {
                return confirm("Are you sure you want to map patient with pepfar Id (" + pepfarId + ") with a new card?")
            }
        }))
    }

    function getPatientDetails(patientIdentifier) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("cdrsync", "nfc", "getPatientDetails")}",
            dataType: "json",
            data: {
                'patientIdentifier': patientIdentifier
            }
        }))
    }

    function syncCustom(from, to, total, start, length, id) {
        return Promise.resolve(jq.ajax({
            url: "${ui.actionLink("getPatientsFromCustomDate")}",
            dataType: "json",
            data: {
                'from': from,
                'to': to,
                'start': start,
                'length': length,
                'total': total,
                'id': id
            }
        }))
    }

    function saveSyncDate() {
        alert("Saving last sync")
        jq.ajax({
            url: "${ui.actionLink("saveLastSync")}",
            dataType: "json"
        })
    }

    function updateCdrSyncBatch(processed, type, id, total) {
        jq.ajax({
            url: "${ui.actionLink("updateCdrSyncBatch")}",
            dataType: "json",
            data: {
                'processed': processed,
                'type': type,
                'id': id,
                'total': total
            }
        })
    }

    function makeId(length) {
        let result = '';
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        const charactersLength = characters.length;
        let counter = 0;
        while (counter < length) {
            result += characters.charAt(Math.floor(Math.random() * charactersLength));
            counter += 1;
        }
        return result;
    }
</script>