let todayTab = $('#today-tab');
let forecastTab = $('#forecast-tab');
let currentWeather = $('#weather-current');
let forecastWeather = $('#weather-forecast');
let hourlyWeather = $('#weather-hourly');
let nearbyWeather = $('#weather-nearby');
let searchError = $('#weather-notfound');

export class Controller {

  #selectedTab = 'current';

  #toggleOn = (button) => button.css({
    'pointer-events': 'none',
    'border-left-color': 'rgb(90, 88, 94)',
    'border-right-color': 'rgb(90, 88, 94)',
    'border-bottom-color': 'rgb(235, 77, 103)',
  });

  #toggleOff = (button) => button.css({
    'pointer-events': 'auto',
    'border-left-color': 'transparent',
    'border-right-color': 'transparent',
    'border-bottom-color': 'transparent'
  });

  setForecastDay(index) {
    if (index >= 1 && index <= 5) {
      for (let i = 1; i <= 5; i++)
        if (i !== index ) {
          $(`#day-${i}-btn`).css({
            'pointer-events': 'auto',
            'background-color': 'rgb(48, 58, 77)'
          });
          $(`#day-${i}-table`).css('display', 'none');
        }
      $(`#day-${index}-btn`).css({
        'pointer-events': 'none',
        'background-color': 'rgb(80, 96, 128)'
      });
      $(`#day-${index}-table`).css('display', 'block');
    }
  }

  animateVisit() {
    this.setForecastDay(1);
    searchError.show('drop', {direction: 'down'}, 'slow');
    hourlyWeather.fadeIn('slow');
    currentWeather.show('drop', {direction: 'left'}, 'slow').promise().done(() => {
      nearbyWeather.show('drop', {direction: 'down'}, 'fast')
    });
  }

  switchTab() {
    this.setForecastDay(1);
    if (this.#selectedTab === 'current') {
      this.#selectedTab = 'forecast';
      this.#toggleOff(todayTab);
      this.#toggleOn(forecastTab);
      document.title = 'Forecaster - 5-day forecast';
      nearbyWeather.hide('drop', {direction: 'down'}, 'fast');
      currentWeather.hide('drop', {direction: 'left'}, 'fast').promise().done(() => {
        forecastWeather.show('drop', {direction: 'left'}, 'fast').promise().done(() => {
          forecastWeather.css('display', 'flex');
        });
      });
    } else {
      this.#selectedTab = 'current';
      this.#toggleOff(forecastTab);
      this.#toggleOn(todayTab);
      document.title = 'Forecaster - Today';
      forecastWeather.hide('drop', {direction: 'left'}, 'fast').promise().done(() => {
        currentWeather.show('drop', {direction: 'left'}, 'fast').promise().done(() => {
          nearbyWeather.show('drop', {direction: 'down'}, 'fast');
        });
      });
    }
  }

}