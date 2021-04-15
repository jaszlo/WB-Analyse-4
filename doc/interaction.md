# Add new interaction to database

The given interaction is added to the database. An interaction can happen between users and between users and documents.

**URL** : `/api/interaction`

**Method** : `POST`

**Auth required** : No

## Success Response

**Code** : `200 OK`

**Required Body**
```
{
  "names":["id1", "id2", "id3", ...],
  "datetime": UNIX Timestamp in ms,
  "duration": Lenght of interaction in ms,
  "document": boolean
}
```

`document` is optional and by default `false`.

* names - Ids of the subjects that interacted
* datetime - Start time of the interaction as an UNIX timestamp in milliseconds (See [currentmillis](https://currentmillis.com/tutorials/system-currentTimeMillis.html))
* duration - The duration of the interaction in milliseconds
* document - If set to true the last id in the interaction is interpreted as an id of a document. The interaction is interpreted as a read/write operation on a document.


For all options but FLOW_DISTANCE the edge weights are normalized.

**Data example**
```json
{
  "names": ["007", "005"],
  "datetime": 1611081858816,
  "duration": 10000,
  "document": false
}
```


**Content examples**

The amount of subjects that participated in the interaction.

```json
{
  "added": 2
}
```

## Notes

This route was much fun to write :)
