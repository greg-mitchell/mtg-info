$(function() {
    $( "#name" ).autocomplete({
      source: "/indices/placings/name"
    });
    $( "#deck" ).autocomplete({
      source: "/indices/placings/deck"
    });
    $( "#event" ).autocomplete({
      source: "/indices/placings/event"
    });
    $( "#location" ).autocomplete({
      source: "/indices/placings/location"
    });
    $( "#date" ).autocomplete({
      source: "/indices/placings/date"
    });
    $( "#format" ).autocomplete({
      source: "/indices/placings/format"
    });
    $( "#place" ).autocomplete({
      source: "/indices/placings/place"
    });
});
