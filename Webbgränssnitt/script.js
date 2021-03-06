function getLocation() {
    var inputAmount = $('#toAmount').val();
    if(inputAmount == "" || inputAmount == null){
        alert("Select a valid amount");
        return;
    }
    else if(!Number.isInteger(+inputAmount)){
        alert("Select a valid amount");
        return
    }
    var userInput = $('#location').val();
    if (userInput == null || userInput == "") {
        if (navigator.geolocation) {
            navigator.geolocation.getLocation
            navigator.geolocation.getCurrentPosition(showPosition, failedToRetrieve);
        }
        else {
            $('#type').show();
            $('#location').show();
            $('#titletext').text("Select your location, amount and preferred currency.");
        }
    }
    else {
        //do check for city or country
        //then do functions for either, country is priority
        var locationInput = $("#location").val();
        var locationType = $("#type").val();
        
        if(locationType == "Country"){
            getAmountWithCountry(locationInput);
        }
        else if (locationType == "City"){
            getAmountWithCity(locationInput);
        }   
    }
}

function showPosition(position) {
    $('#type').hide();
    $('#location').hide();
    $('#location').val("");
    $('#titletext').text("Select your amount and preferred currency.");
    var amount = $('#toAmount').val();
    var preffCurr = $('#currency').val().toUpperCase();
    
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&lat=' + position.coords.latitude + '&lon=' + position.coords.longitude + '&includeBMI=true',
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['originalCurrency'];
            var convAmount = Math.round(data['convertedAmount']*100)/100;
            $("#outcome").text("Amount: " + convAmount+ " " + preffCurr + " from " + amount +  " " + convCurr);
            var bmiExist = data["bmiDataAvailable"];
            $("#bmiCustom").html("Estimate in " +preffCurr);
            if(bmiExist != null && bmiExist == true){
                var adjustedAmount = Math.round(data['bmiAdjustedAmount']*100)/100;
                var adjustedAmountCountry = data['bmiAdjustedAmountCountry'];
                //$("#outcome").html("Amount: " + convAmount+ " " + preffCurr + " from " + amount +  " " + convCurr + "<br>What the same product might cost in " +adjustedAmountCountry + ": " + adjustedAmount+ " " +preffCurr);
                $("#outcome").append("<br>What the same product might cost in " +adjustedAmountCountry + ": " + adjustedAmount+ " " +preffCurr);
                $("#tableBMI tbody").empty();
                var bmiString = "";
                
                for (i = 0; i < data.cityComparison.length; i++){
                    var bmiCity = data.cityComparison[i].city;
                    var bmiCountry = data.cityComparison[i].country;
                    var prefCurrencyAmount = Math.round(data.cityComparison[i].prefCurrencyAmount*100)/100;
                    var dollarAmount = Math.round(data.cityComparison[i].dollarAmount*100)/100;
                    bmiString = bmiString + bmiCity+", "+bmiCountry+" "+prefCurrencyAmount+" " +preffCurr + " $"+dollarAmount + "<br>\n";
                    //$("#tableBMI").find(".table").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                    $("#tableBMI").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                }
                $("#box3").show();
                console.log(bmiString);
                //$("#bigmac").html(bmiString);
            }
            else {
                $("#box3").hide();
                $("#tableBMI tbody").empty();
            }
            console.log(prefCurr + "\n" + convCurr + "\n" + amount);
			console.log(data);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
            //console.log(jqHXR);
            console.log(textStatus);
            console.log(error);
        });
}

function getAmountWithCountry(country){
    var amount = $('#toAmount').val();
    var preffCurr = $('#currency').val().toUpperCase();
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&country='+country + '&includeBMI=true',
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['originalCurrency'];
            var convAmount = Math.round(data['convertedAmount']*100)/100;
            $("#outcome").text("Amount: " + convAmount+ " " + preffCurr + " from " + amount +  " " + convCurr);
            var bmiExist = data["bmiDataAvailable"];
            $("#bmiCustom").html("Estimate in " +preffCurr);
            if(bmiExist != null && bmiExist == true){
                var adjustedAmount = Math.round(data['bmiAdjustedAmount']*100)/100;
                var adjustedAmountCountry = data['bmiAdjustedAmountCountry'];
                $("#outcome").append("<br>What the same product might cost in " +adjustedAmountCountry + ": " + adjustedAmount+ " " +preffCurr);                
                $("#tableBMI tbody").empty();
                var bmiString = "";
                
                for (i = 0; i < data.cityComparison.length; i++){
                    var bmiCity = data.cityComparison[i].city;
                    var bmiCountry = data.cityComparison[i].country;
                    var prefCurrencyAmount = Math.round(data.cityComparison[i].prefCurrencyAmount*100)/100;
                    var dollarAmount = Math.round(data.cityComparison[i].dollarAmount*100)/100;
                    bmiString = bmiString + bmiCity+", "+bmiCountry+" "+prefCurrencyAmount+" " +preffCurr + " $"+dollarAmount + "<br>\n";
                    //$("#tableBMI").find(".table").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                    $("#tableBMI").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                }
                $("#box3").show();
                //console.log(bmiString);
                //$("#bigmac").html(bmiString);
            }
            else {
                $("#box3").hide();
                $("#tableBMI tbody").empty();
            }
            //console.log(prefCurr + "\n" + convCurr + "\n" + amount);
			//console.log(data);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
            //console.log(jqHXR);
            //console.log(textStatus);
            //console.log(error);
        });
}

function getAmountWithCity(city){
    var amount = $('#toAmount').val();
    var preffCurr = $('#currency').val().toUpperCase();
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&city='+city + '&includeBMI=true',
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['originalCurrency'];
            var convAmount = Math.round(data['convertedAmount']*100)/100;
            $("#outcome").text("Amount: " + convAmount+ " " + preffCurr + " from " + amount +  " " + convCurr);
            var bmiExist = data["bmiDataAvailable"];
            $("#bmiCustom").html("Estimate in " +preffCurr);
            if(bmiExist != null && bmiExist == true){
                var adjustedAmount = Math.round(data['bmiAdjustedAmount']*100)/100;
                var adjustedAmountCountry = data['bmiAdjustedAmountCountry'];
                $("#outcome").append("<br>What the same product might cost in " +adjustedAmountCountry + ": " + adjustedAmount+ " " +preffCurr);
                $("#tableBMI tbody").empty();
                var bmiString = "";
                
                for (i = 0; i < data.cityComparison.length; i++){
                    var bmiCity = data.cityComparison[i].city;
                    var bmiCountry = data.cityComparison[i].country;
                    var prefCurrencyAmount = Math.round(data.cityComparison[i].prefCurrencyAmount*100)/100;
                    var dollarAmount = Math.round(data.cityComparison[i].dollarAmount*100)/100;
                    bmiString = bmiString + bmiCity+", "+bmiCountry+" "+prefCurrencyAmount+" " +preffCurr + " $"+dollarAmount + "<br>\n";
                    //$("#tableBMI").find(".table").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                    $("#tableBMI").append("<tr><td>"+bmiCity+"</td><td>"+bmiCountry+"</td><td>"+prefCurrencyAmount+"</td><td>"+dollarAmount+"</td></tr>");
                }
                $("#box3").show();
                //console.log(bmiString);
                //$("#bigmac").html(bmiString);
            }
            else {
                $("#box3").hide();
                $("#tableBMI tbody").empty();
            }
            //console.log(prefCurr + "\n" + convCurr + "\n" + amount);
			//console.log(data);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
            //console.log(jqHXR);
            //console.log(textStatus);
            //console.log(error);
        });
}

function failedToRetrieve(position) {
    $('#type').show();
    $('#location').show();
    $('#titletext').text("Select your location, amount and preferred currency.");
}

$(document).ready(function(){
    $('#type').hide();
    $('#location').hide();
    $("#box3").hide();
});