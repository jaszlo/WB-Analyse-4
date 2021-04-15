# Get all user IDs

Get a list of all current registered user IDs stored in the database.

**URL** : `/api/ids`

**Method** : `GET`

**Auth required** : No

## Success Response

**Code** : `200 OK`

**Content examples**

If all the ids in the database are `1234, 9612, 2143, 9212, 1246, 9312` the reply would look like this.

```json
[
  "1234",
  "9612",
  "2143",
  "9212",
  "1246",
  "9312"
]
```

## Notes

This route was much fun to write :)