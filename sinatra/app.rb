# frozen_string_literal: true

require 'sinatra'
require 'sinatra/json'
require 'net/http'

get '/' do
  send_file File.join(settings.public_folder, 'index.html')
end

get '/token_generator' do
  res = Net::HTTP.post(URI('https://api-js-qa.drugbankplus.com/v1/tokens'), {ttl: 24}.to_json, 'Authorization': 'ba073a81cc0de1d1f9be08d5a7092317', 'Content-Type': 'application/json', 'Cache-Control': 'no-cache')
  res.body
end