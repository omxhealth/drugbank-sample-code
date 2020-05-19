# frozen_string_literal: true

require 'sinatra'
require 'sinatra/json'
require 'net/http'

get '/' do
  send_file File.join(settings.public_folder, 'index.html')
end

get '/token_generator' do
  res = Net::HTTP.post(URI('https://api-js-qa.drugbankplus.com/v1/tokens'), {ttl: 24}.to_json, 'Authorization': ENV['AUTH_TOKEN'], 'Content-Type': 'application/json', 'Cache-Control': 'no-cache')
  res.body
end