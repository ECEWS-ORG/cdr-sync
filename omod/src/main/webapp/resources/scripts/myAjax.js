function myAjax(data, cohortAjaxUrl)
{
    let myPromise = new Promise(function(resolve, reject) {
    // "Producing Code" (May take some time)

      jq = jQuery;
   
        jq.getJSON(cohortAjaxUrl,
            
        data, function(data){
                resolve(data)
            }, function(xhr, status, error){
                console.log(error);
                reject(error)
         })
         
    });
    
    return myPromise;
          
}