package com.botdarr.api.radarr;

public class RadarrUrls {
  /**
   * The base download(s) url for get, put, delete requests (which each do different things in radarr)
   * See https://github.com/Radarr/Radarr/wiki/API:Queue
   */
  public static final String DOWNLOAD_BASE = "queue";

  /**
   * The base movie url for getting and adding movies (get, post requests)
   * See https://github.com/Radarr/Radarr/wiki/API:Movie
   */
  public static final String MOVIE_BASE = "movie";

  /**
   * See https://github.com/Radarr/Radarr/wiki/API:Movie-Lookup
   */
  public static final String MOVIE_LOOKUP = "movie/lookup";

  /**
   * See https://github.com/Radarr/Radarr/wiki/API:Movie-Lookup
   */
  public static final String MOVIE_LOOKUP_TMDB = "movie/lookup/tmdb";

  /**
   * The url for triggering gets requests in radarr to discover new movies
   */
  public static final String DISCOVER_MOVIES = "importlist/movie";

  /**
   * The url base for adding, getting, deleting movie profiles
   */
  public static final String PROFILE_BASE = "/qualityProfile";
}
