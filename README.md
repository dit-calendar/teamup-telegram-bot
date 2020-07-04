# teamup-telegram-bot [![Build Status](https://travis-ci.org/dit-calendar/teamup-telegram-bot.svg?branch=master)](https://travis-ci.org/dit-calendar/dit-calendar-bot)

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/dit-calendar/teamup-telegram-bot/tree/master)

# deployment
* `gradle build`
* `heroku deploy:jar build/libs/teamup-telegram-bot*-all.jar --app teamup-telegram-bot`

# manual test
* https://core.telegram.org/bots/webhooks
* check bot status `https://api.telegram.org/bot{token}/getWebhookInfo`
* send message manually
 `curl -v -k -X POST -H "Content-Type: application/json" -H "Cache-Control: no-cache"  -d '{
 "update_id":10000,
 "message":{
   "date":1441645532,
   "chat":{
      "last_name":"Test Lastname",
      "id":1111111,
      "first_name":"Test",
      "username":"Test"
   },
   "message_id":1365,
   "from":{
      "last_name":"Test Lastname",
      "id":1111111,
      "first_name":"Test",
      "username":"Test"
   },
   "text":"/start"
 }
 }' "localhost:8443/"`
