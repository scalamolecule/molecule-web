


$(document).ready(function() {
  $("#sidebarCollapse").on("click", function() {
    $("#sidebar").toggleClass("active");
    $(this).toggleClass("active");
  });
});

$(document).ready(function() {
  $("#sidebar").on("click", function() {
    $(this).toggleClass("active");
  });
});