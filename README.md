<p align="center"><img src="frontend/Googlemaps/app/src/main/res/drawable/new_logo.png" width="400" /></p>

# group-23
**Groupwork for CS 196 Group 23**

A COVID-19 map which displays data on where most of the cases are concentrated by superimposing cases on google maps or through a purely diagrammatic representation of the cases.   While there may be other applications/ services that provide covid-19 trackers, our app stands out in the aspect that we aim to provide data accurate for the locality that the person uses the tracker in. In particular, it is most important to get this app working for the Urbana-Champaign area.


# Backend API Overview
Notes:
* Consistent with [https://covid-23.herokuapp.com/](https://covid-23.herokuapp.com/).
    * Use `git push heroku master` to update heroku!

## News
### Request
`GET /news/`
### Response
* `result.date` is the current date
* `result.locations` is a dictionary of locations with keys of the form `US-<2 letter state code>`, as in `US-MA`
* `use_cached` is `true` if we used the cache on heroku (this should only happen once per day globally)

## Contact Tracing
### Request
`GET /get_new_code/`
### Response
`XP0SLBENH79ORLQOZXYUJFVE`, a string that can be stored locally
<hr/>

### Request
`POST /check_compromised/`
Body:
```angular2
{ "codes": ["all", "my", "codes", "in", "the", "past", "few", "days"] }
```
### Response
* `result` is `true` if we've been near someone with covid, `false` otherwise

### Request
`POST /post_location/`

# Backend CLI
```
> python backend/champaign/scrapers.py
> python backend/nation_state/nation_state_data_plotter.py
```
The data will be in `backend/plt`
