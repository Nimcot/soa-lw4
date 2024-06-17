import { Controller } from './lib.js';


$(document).ready(() => {

  // 0:
  const controller = new Controller();

  //->
  let logo = $('#heading > div > img');
  let forecastWeather = $('#weather-forecast');

  // 1:
  forecastWeather.css('display', 'flex');
  forecastWeather.hide(1);

  // 2:
  $('#today-tab, #forecast-tab').click(() => controller.switchTab());

  // 3:
  $('#heading').hover(() => {
      if ($('html').width() >= 576)
        logo.css('transform', 'rotate(270deg)');
    }, () => {
      if ($('html').width() >= 576)
        logo.css('transform', 'rotate(0deg)');
    }
  );

  // 4:
  for (let i = 1, btn; i <= 5; i++) {
    btn = `#day-${i}-btn`;
    forecastWeather
        .on('click', btn, () => controller.setForecastDay(i))
        .on('mouseenter', btn, () => $(btn).css('background-color', 'rgb(100, 110, 131)'))
        .on('mouseleave', btn, () => $(btn).css('background-color', $(btn).css('pointer-events') === 'none' ? 'rgb(80, 96, 128)' : 'rgb(48, 58, 77)'));
  }

  // 5:
  controller.animateVisit();

});