
function getLocation() {
    var inputAmount = $('#toAmount').val();
    if(inputAmount == "" || inputAmount == null){
        alert("Select a correct amount");
        return;
    }
    else if(!Number.isInteger(+inputAmount)){
        alert("Select a correct amount");
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
            $('#titletext').text("Select your location, preffered currency and amount");
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
    $('#titletext').text("Select your preffered currency and amount.");
    var amount = $('#toAmount').val();
    var preffCurr = $('#toSelect').val();
    
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&lat=' + position.coords.latitude + '&lon=' + position.coords.longitude,
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            console.log(data.textStatus);
            var prefCurr = data['prefCurrency'];
            var convCurr = data['currencyConverted'];
            var amount = data['returnAmount'];
            $("#outcome").text("Amount: " + amount+ " " + preffCurr);
            console.log(prefCurr + "\n" + convCurr + "\n" + amount);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
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

function getAmountWithCountry(country){
    var amount = $('#toAmount').val();
    var preffCurr = $('#toSelect').val();
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&country='+country,
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['currencyConverted'];
            var amount = data['returnAmount'];
            $("#outcome").text("Amount: " + amount+ " " + preffCurr);
            console.log(prefCurr + "\n" + convCurr + "\n" + amount);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
            //console.log(jqHXR);
            console.log(textStatus);
            console.log(error);
        });
}

function getAmountWithCity(city){
    var amount = $('#toAmount').val();
    var preffCurr = $('#toSelect').val();
    $.ajax({
        url: 'http://localhost:8192' + '/api/v1/conversion?preferredCurrency='+preffCurr+'&amount='+amount + '&city='+city,
        method: "GET",
        headers: { "Accept": "application/json" }
    })
        .done(function (data) {

            //x.innerHTML = "Latitude: " + position.coords.latitude +
            //    "<br>Longitude: " + position.coords.longitude;
            var prefCurr = data['prefCurrency'];
            var convCurr = data['currencyConverted'];
            var amount = data['returnAmount'];
            $("#outcome").text("Amount: " + amount+ " " + preffCurr);
            console.log(prefCurr + "\n" + convCurr + "\n" + amount);

        })
        .fail(function (jqXHR, textStatus, error) {
            alert("Failed to retrieve the currency, try again.");
            //console.log(jqHXR);
            console.log(textStatus);
            console.log(error);
        });
}

function failedToRetrieve(position) {
    $('#type').show();
    $('#location').show();
    $('#titletext').text("Select your location, preffered currency and amount");
}

$(document).ready(function(){
    $('#type').hide();
    $('#location').hide();
});