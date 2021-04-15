# Shutdown the server

Route for shutting down the server

**URL** : `/shutdown`

**Method** : `ANY`

**Auth required** : Yes

## Success Response

**Code** : `200 OK`

**Content examples**

This route always returns this message.

```text
[TERMINATING] Shutting down the Server. This will take a few seconds.
```

After that, no new connections are allowed and after a few seconds the server terminates.

## Notes

This route was much fun to write :)