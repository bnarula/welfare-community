// Preloader //

jQuery(document).ready(function($) {  

/*$('#banner').on('load', function(){*/
  $('#preloader').fadeOut('slow',function(){$(this).remove();});
/*});*/

});

new WOW().init();


$(window).on('scroll', function(){
  var sticky = $('.sticky'),
      scroll = $(window).scrollTop();

  if (scroll >= 100) sticky.addClass('fixed');
  else sticky.removeClass('fixed');
});


