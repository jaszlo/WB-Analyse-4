# Generates the interaction graph

Generates a graph based on the interaction data in the database. The edge weights are between zero and one except for the FLOW distance graph.

**URL** : `/api/graph`

**Method** : `GET`, `POST`

**Auth required** : No

## Success Response

**Code** : `200 OK`

**Required Body (`POST` only)**
```json
{
  "option": "One of [INTERACTION_SUM, DURATION_SUM, INTERACTION_TIMES_DURATION, INVERTED_INTERACTION_SUM, INVERTED_DURATION_SUM, INVERTED_INTERACTION_TIMES_DURATION, FLOW_DISTANCE]"
}
```
The `option` dictates how the graph is generated. The edge weights are calculated by:
* INTERACTION_SUM - The count of all interactions
* INVERTED_INTERACTION_SUM - One divided by the count of all interactions
* DURATION_SUM - The sum of all interaction durations
* INVERTED_DURATION_SUM - One divided by the sum of all interaction durations
* INTERACTION_TIMES_DURATION - The multiplication of the count of all interactions times the sum of all interaction durations
* INVERTED_INTERACTION_TIMES_DURATION  - One divided by the multiplication of the count of all interactions times the sum of all interaction durations
* FLOW_DISTANCE - The FLOW distance

between two people.

For all options but FLOW_DISTANCE the edge weights are normalized.  

**Data example**
```json
{
  "option": "INTERACTION_SUM"
}
```

**Required Body (`GET` only)**

Since the `GET` request does not have a body, the default option for generating the graph is used. This default option is set in the config file of the server.

**Content examples**

![Generated graph](resources/exampleGraph.svg)
If this graph was generated, the output would be as follows:

```json
{
  "a1": {"a2": 1, "b1": 0.25, "c1": 0.25},
  "b2": {"a2": 0.25, "c2": 0.5, "d2": 0.75, "e2": 0.25},
  "a2": {"b2": 0.25, "a1": 1, "d2": 0.25, "e2": 0.25, "c2": 0.25},
  "d1": {"c1": 0.25},
  "e2": {"a2": 0.25, "b2": 0.25, "c2": 0.75, "d2": 0.25},
  "c1": {"d1": 0.25, "a1": 0.25, "b1": 0.75},
  "d2": {"a2": 0.25, "c2": 0.25, "b2": 0.75, "e2": 0.25},
  "b1": {"a1": 0.25, "c1": 0.75},
  "c2": {"a2": 0.25, "b2": 0.5, "d2": 0.25, "e2": 0.75}
}
```

The graph is given as an adjacency list. So e.g. d1 -> c1 -> 0.25 means that d1 is connected to c1 with a weight of one fourth.

## Notes

This route was much fun to write :)
