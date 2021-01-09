var x = document.getElementById("coords");
	function getLocation() {
        var userInput = $("#countryForm input[name=countryUser]").val();
        if(userInput == null || userInput == ""){
            if (navigator.geolocation) {
		    navigator.geolocation.getLocation
			navigator.geolocation.getCurrentPosition(showPosition, failedToRetrieve);
		    } 
		    else {
		        x.innerHTML = "Geolocation is not supported by this browser.";
		    } 
        }
        else{
            //do check for city or country
            //then do functions for either, country is priority
            console.log("poopoo");
        }
        
        
		
	}

	function showPosition(position) {
        $('form').hide();
        $.ajax({
            url: 'http://localhost:8192' +'/api/v1/conversion?preferredCurrency=USD&amount=100'+'&lat='+position.coords.latitude + '&lon='+position.coords.longitude,
            method: "GET",
            headers: {"Accept": "application/json"}
        })
        .done(function(data){

            x.innerHTML = "Latitude: " + position.coords.latitude +
            "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['currencyConverted'];
            var amount = data['returnAmount'];
            console.log(prefCurr + "\n" + currencyConverted+ "\n"+amount);
        
        })
        .fail(function(jqXHR, textStatus, error){
            //console.log(jqHXR);
            console.log(textStatus);
            console.log(error);
        });
        /*
        $('form').hide();
        $.ajax({
            url: 'https://geocode.xyz/'+position.coords.latitude+','+position.coords.longitude+'?json=1',
            method: "GET",
            headers: {"Accept": "application/json"}
        })
        .done(function(data){
            x.innerHTML = "Latitude: " + position.coords.latitude +
            "<br>Longitude: " + position.coords.longitude +
            "<br>Country: " + data['country'];
        })
        .fail(function(jqXHR, textStatus, error){
            console.log(jqHXR);
            console.log(textStatus);
            console.log(error);
        });
        */
        
        /*
		$.get("https://geocode.xyz/"+position.coords.latitude+","+position.coords.longitude+"?json=1", function (response) {
            
            x.innerHTML = "Latitude: " + position.coords.latitude +
            "<br>Longitude: " + position.coords.longitude +
            "<br>Country: " + response.country;
        }, "jsonp");
        */

    }
    
	function failedToRetrieve(position) {
		x.innerHTML = "You didn't give access to your location, type the country below";
        $('#countryForm').show();
    }
    $(document).ready(function(){
        $('form').hide();
    });