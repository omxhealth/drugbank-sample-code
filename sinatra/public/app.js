$(document).ready(function(){
  token = get_token()
  $("#search").click(async function(){
    let t = await token

    let url = 'http://api-js.drugbank.local:3001/v1/us/indications?q=' + $("#query").val();

    let data = await api_request(url, t)

    data.forEach( e => {
        $("#results").append( $("<p>" + e['drug']['name'] + "</p>" ));
    });  
  })
})

function api_request(url, token){
  return new Promise(function(resolve, reject){
    let xhr = new XMLHttpRequest();
    xhr.open('GET', url)
    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    xhr.onload = function() {
      if (this.status == 200){
        resolve(JSON.parse((xhr.response)))
      } else {
        reject({
          status: this.status,
          statusText: xhr.statusText
        });
      }
    };
    xhr.onerror = function () {
      reject({
          status: this.status,
          statusText: xhr.statusText
        });
    };
    xhr.send();
  });
}

function get_token(){
  return new Promise(function(resolve, reject){
    let xhr = new XMLHttpRequest();
    xhr.open('GET','token_generator');
    xhr.onload = function() {
      if (this.status == 200){
        resolve(JSON.parse((xhr.response))['token'])
      } else {
        reject({
          status: this.status,
          statusText: xhr.statusText
        });
      }
    };
    xhr.onerror = function () {
      reject({
          status: this.status,
          statusText: xhr.statusText
        });
    };
    xhr.send();
  });
}