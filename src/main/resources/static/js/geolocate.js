$(document).ready(() => {
    if (navigator.geolocation)
        navigator.geolocation.getCurrentPosition((position) => {
            let form = document.createElement("form");
            form.method = "POST";
            form.action = "/";
            let latField = document.createElement("input");
            latField.type = "hidden";
            latField.name = "lat";
            latField.value = position.coords.latitude;
            let lonField = document.createElement("input");
            lonField.type = "hidden";
            lonField.name = "lon";
            lonField.value = position.coords.longitude;
            form.appendChild(latField);
            form.appendChild(lonField);
            document.body.appendChild(form);
            form.submit();
        });
});