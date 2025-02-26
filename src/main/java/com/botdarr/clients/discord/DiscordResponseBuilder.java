package com.botdarr.clients.discord;

import com.botdarr.Config;
import com.botdarr.api.lidarr.LidarrArtist;
import com.botdarr.api.lidarr.LidarrCommands;
import com.botdarr.api.lidarr.LidarrQueueRecord;
import com.botdarr.api.lidarr.LidarrQueueStatusMessage;
import com.botdarr.api.sonarr.*;
import com.botdarr.clients.ChatClientResponseBuilder;
import com.botdarr.api.radarr.*;
import com.botdarr.commands.*;
import com.botdarr.commands.responses.*;
import com.botdarr.utilities.ListUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.logging.log4j.util.Strings;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.botdarr.api.lidarr.LidarrApi.ADD_ARTIST_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.radarr.RadarrApi.ADD_MOVIE_COMMAND_FIELD_PREFIX;
import static com.botdarr.api.sonarr.SonarrApi.ADD_SHOW_COMMAND_FIELD_PREFIX;
import static com.botdarr.commands.StatusCommand.STATUS_COMMAND;
import static com.botdarr.commands.StatusCommand.STATUS_COMMAND_DESCRIPTION;
import static net.dv8tion.jda.api.entities.MessageEmbed.VALUE_MAX_LENGTH;

public class DiscordResponseBuilder implements ChatClientResponseBuilder<DiscordResponse> {
  @Override
  public DiscordResponse build(HelpResponse helpResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Commands");
    try {
      embedBuilder.setDescription(ChatClientResponseBuilder.getVersion());
    } catch (IOException e) {
      throw new RuntimeException("Error getting botdarr version", e);
    }
    boolean radarrEnabled = Config.isRadarrEnabled();
    boolean sonarrEnabled = Config.isSonarrEnabled();
    boolean lidarrEnabled = Config.isLidarrEnabled();
    if (radarrEnabled) {
      embedBuilder.addField(RadarrCommands.getHelpMovieCommandStr(), "Shows all the commands for movies", false);
    }

    if (sonarrEnabled) {
      embedBuilder.addField(SonarrCommands.getHelpShowCommandStr(), "Shows all the commands for shows", false);
    }

    if (lidarrEnabled) {
      embedBuilder.addField(LidarrCommands.getHelpCommandStr(), "Shows all the commands for music", false);
    }

    if (!radarrEnabled && !sonarrEnabled && !lidarrEnabled) {
      embedBuilder.appendDescription("No radarr or sonarr or lidarr commands configured, check your properties file and logs");
    }
    if (!Config.getStatusEndpoints().isEmpty()) {
      embedBuilder.addField(new CommandProcessor().getPrefix() + STATUS_COMMAND, STATUS_COMMAND_DESCRIPTION, false);
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MusicHelpResponse musicHelpResponse) {
    return getListOfCommands(musicHelpResponse.getLidarrCommands());
  }

  @Override
  public DiscordResponse build(MoviesHelpResponse moviesHelpResponse) {
    return getListOfCommands(moviesHelpResponse.getRadarrCommands());
  }

  @Override
  public DiscordResponse build(ShowsHelpResponse showsHelpResponse) {
    return getListOfCommands(showsHelpResponse.getSonarrCommands());
  }

  @Override
  public DiscordResponse build(ShowResponse showResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    SonarrShow show = showResponse.getShow();
    embedBuilder.setTitle(show.getTitle());
    embedBuilder.addField("TvdbId", String.valueOf(show.getTvdbId()), false);
    embedBuilder.addField(ADD_SHOW_COMMAND_FIELD_PREFIX, SonarrCommands.getAddShowCommandStr(show.getTitle(), show.getTvdbId()), false);
    embedBuilder.setImage(show.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MusicArtistResponse musicArtistResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    LidarrArtist lidarrArtist = musicArtistResponse.getArtist();
    embedBuilder.setTitle(lidarrArtist.getArtistName());
    embedBuilder.addField("Id", String.valueOf(lidarrArtist.getForeignArtistId()), false);
    embedBuilder.addField(ADD_ARTIST_COMMAND_FIELD_PREFIX, LidarrCommands.getAddArtistCommandStr(lidarrArtist.getArtistName(), lidarrArtist.getForeignArtistId()), false);
    embedBuilder.setImage(lidarrArtist.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ShowDownloadResponse showDownloadResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    SonarrQueue showQueue = showDownloadResponse.getShowQueue();
    SonarQueueEpisode episode = showQueue.getEpisode();
    embedBuilder.setTitle(showQueue.getSonarrQueueShow().getTitle());
    embedBuilder.addField("Season/Episode", "S" + episode.getSeasonNumber() + "E" + episode.getEpisodeNumber(), true);
    embedBuilder.addField("Quality", showQueue.getQuality().getQuality().getName(), true);
    embedBuilder.addField("Status", showQueue.getStatus(), true);
    embedBuilder.addField("Time Left", showQueue.getTimeleft() == null ? "unknown" : showQueue.getTimeleft(), true);
    String overview = episode.getTitle() + ": " + episode.getOverview();
    if (overview.length() > VALUE_MAX_LENGTH) {
      overview = overview.substring(0, VALUE_MAX_LENGTH);
    }
    embedBuilder.addField("Overview", overview, false);
    if (showQueue.getStatusMessages() != null) {
      for (SonarrQueueStatusMessages statusMessage : showQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          embedBuilder.addField("Download message", message, true);
        }
      }
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MovieDownloadResponse movieDownloadResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    RadarrQueue radarrQueue = movieDownloadResponse.getRadarrQueue();
    embedBuilder.setTitle(radarrQueue.getTitle());
    embedBuilder.addField("Quality", radarrQueue.getQuality().getQuality().getName(), true);
    embedBuilder.addField("Status", radarrQueue.getStatus(), true);
    embedBuilder.addField("Time Left", radarrQueue.getTimeleft() == null ? "unknown" : radarrQueue.getTimeleft(), true);
    if (radarrQueue.getStatusMessages() != null) {
      for (RadarrQueueStatusMessages statusMessage : radarrQueue.getStatusMessages()) {
        for (String message : statusMessage.getMessages()) {
          embedBuilder.addField("Download message", message, true);
        }
      }
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MusicArtistDownloadResponse musicArtistDownloadResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    LidarrQueueRecord lidarrQueueRecord = musicArtistDownloadResponse.getQueueRecord();
    embedBuilder.setTitle(lidarrQueueRecord.getTitle());
    embedBuilder.addField("Time Left", lidarrQueueRecord.getTimeleft() == null ? "unknown" : lidarrQueueRecord.getTimeleft(), true);
    embedBuilder.addField("Status", lidarrQueueRecord.getStatus(), true);
    if (lidarrQueueRecord.getStatusMessages() != null) {
      for (LidarrQueueStatusMessage statusMessage : ListUtils.subList(lidarrQueueRecord.getStatusMessages(), 5)) {
        for (String message : statusMessage.getMessages()) {
          embedBuilder.addField("Download message", message, false);
        }
      }
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ShowProfileResponse showProfileResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Profile");
    SonarrProfile sonarrProfile = showProfileResponse.getShowProfile();
    embedBuilder.addField("Name", sonarrProfile.getName(), false);
    embedBuilder.addField("Cutoff", sonarrProfile.getCutoff().getName(), false);
    embedBuilder.addBlankField(false);
    for (int k = 0; k < sonarrProfile.getItems().size(); k++) {
      SonarrProfileQualityItem sonarrProfileQualityItem = sonarrProfile.getItems().get(k);
      if (sonarrProfileQualityItem.isAllowed()) {
        embedBuilder.addField(
          "Quality",
          "name=" + sonarrProfileQualityItem.getQuality().getName() + ", resolution=" + sonarrProfileQualityItem.getQuality().getResolution(),
          true);
      }
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MovieProfileResponse movieProfileResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Profile");
    RadarrProfile radarrProfile = movieProfileResponse.getRadarrProfile();
    embedBuilder.addField("Name", radarrProfile.getName(), false);
    embedBuilder.addField("Cutoff", "" + radarrProfile.getCutoff(), false);
    embedBuilder.addBlankField(false);
    for (int k = 0; k < radarrProfile.getItems().size(); k++) {
      RadarrProfileQualityItem radarrProfileQualityItem = radarrProfile.getItems().get(k);
      if (radarrProfileQualityItem.isAllowed() && radarrProfileQualityItem.getQuality() != null) {
        embedBuilder.addField(
          "Quality",
          "name=" + radarrProfileQualityItem.getQuality().getName() + ", resolution=" + radarrProfileQualityItem.getQuality().getResolution(),
          true);
      }
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(NewShowResponse newShowResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    SonarrShow sonarrShow = newShowResponse.getNewShow();
    embedBuilder.setTitle(sonarrShow.getTitle());
    embedBuilder.addField("TvdbId", "" + sonarrShow.getTvdbId(), true);
    embedBuilder.addField(ADD_SHOW_COMMAND_FIELD_PREFIX, SonarrCommands.getAddShowCommandStr(sonarrShow.getTitle(), sonarrShow.getTvdbId()), false);
    embedBuilder.setImage(sonarrShow.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ExistingShowResponse existingShowResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    SonarrShow existingShow = existingShowResponse.getExistingShow();
    embedBuilder.setTitle(existingShow.getTitle());
    embedBuilder.addField("TvdbId", "" + existingShow.getTvdbId(), true);
    embedBuilder.addField("Id", "" + existingShow.getId(), true);
    if (existingShow.getSeasons() != null) {
      embedBuilder.addField("Number of seasons", "" + existingShow.getSeasons().size(), true);
      for (SonarrSeason sonarrSeason : existingShow.getSeasons()) {
        embedBuilder.addField("",
          "Season#" + sonarrSeason.getSeasonNumber() +
            ",Available Epsiodes=" + sonarrSeason.getStatistics().getEpisodeCount() + ",Total Epsiodes=" + sonarrSeason.getStatistics().getTotalEpisodeCount(), false);
      }
    }
    embedBuilder.setImage(existingShow.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(NewMovieResponse newMovieResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    RadarrMovie radarrMovie = newMovieResponse.getRadarrMovie();
    embedBuilder.setTitle(radarrMovie.getTitle());
    embedBuilder.addField("TmdbId", String.valueOf(radarrMovie.getTmdbId()), false);
    embedBuilder.addField(ADD_MOVIE_COMMAND_FIELD_PREFIX, RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()), false);
    embedBuilder.setImage(radarrMovie.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ExistingMovieResponse existingMovieResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    RadarrMovie radarrMovie = existingMovieResponse.getRadarrMovie();
    embedBuilder.setTitle(radarrMovie.getTitle());
    embedBuilder.addField("TmdbId", String.valueOf(radarrMovie.getTmdbId()), false);
    embedBuilder.addField("Id", String.valueOf(radarrMovie.getId()), false);
    embedBuilder.addField("Downloaded", String.valueOf((radarrMovie.getSizeOnDisk() > 0)), false);
    embedBuilder.addField("Has File", String.valueOf(radarrMovie.isHasFile()), false);
    embedBuilder.setImage(radarrMovie.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(NewMusicArtistResponse newMusicArtistResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    LidarrArtist lookupArtist = newMusicArtistResponse.getLidarrArtist();
    String artistDetail = " (" + lookupArtist.getDisambiguation() + ")";
    embedBuilder.setTitle(lookupArtist.getArtistName() + (Strings.isEmpty(lookupArtist.getDisambiguation()) ? "" :  artistDetail));
    embedBuilder.addField(ADD_ARTIST_COMMAND_FIELD_PREFIX, LidarrCommands.getAddArtistCommandStr(lookupArtist.getArtistName(), lookupArtist.getForeignArtistId()), false);
    embedBuilder.setImage(lookupArtist.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ExistingMusicArtistResponse existingMusicArtistResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    LidarrArtist lookupArtist = existingMusicArtistResponse.getLidarrArtist();
    String artistDetail = " (" + lookupArtist.getDisambiguation() + ")";
    embedBuilder.setTitle(lookupArtist.getArtistName() + (Strings.isEmpty(lookupArtist.getDisambiguation()) ? "" :  artistDetail));
    embedBuilder.setImage(lookupArtist.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(MovieResponse movieResponse) {
    return getMovieResponse(movieResponse.getRadarrMovie());
  }

  @Override
  public DiscordResponse build(DiscoverMovieResponse discoverMovieResponse) {
    return getMovieResponse(discoverMovieResponse.getRadarrMovie());
  }

  @Override
  public DiscordResponse build(StatusCommandResponse statusCommandResponse) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Endpoint Statuses");
    for (Map.Entry<String, Boolean> endpointStatusEntry : statusCommandResponse.getEndpoints().entrySet()) {
      embedBuilder.addField(endpointStatusEntry.getKey(), endpointStatusEntry.getValue() ? "\uD83D\uDFE2" : "\uD83D\uDD34", false);
    }
    return new DiscordResponse(embedBuilder.build());
  }

  @Override
  public DiscordResponse build(ErrorResponse errorResponse) {
    return new DiscordResponse(createErrorMessageEmbed(errorResponse.getErrorMessage()));
  }

  @Override
  public DiscordResponse build(InfoResponse infoResponse) {
    return new DiscordResponse(createInfoMessageEmbed(infoResponse.getInfoMessage()));
  }

  @Override
  public DiscordResponse build(SuccessResponse successResponse) {
    return new DiscordResponse(createSuccessMessageEmbed(successResponse.getSuccessMessage()));
  }

  private DiscordResponse getMovieResponse(RadarrMovie radarrMovie) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(radarrMovie.getTitle());
    embedBuilder.addField("TmdbId", "" + radarrMovie.getTmdbId(), false);
    embedBuilder.addField(ADD_MOVIE_COMMAND_FIELD_PREFIX, RadarrCommands.getAddMovieCommandStr(radarrMovie.getTitle(), radarrMovie.getTmdbId()), false);
    embedBuilder.setImage(radarrMovie.getRemotePoster());
    return new DiscordResponse(embedBuilder.build());
  }

  private MessageEmbed createInfoMessageEmbed(String message) {
    return createMessageEmbed("Info", Color.WHITE, message);
  }

  private MessageEmbed createSuccessMessageEmbed(String message) {
    return createMessageEmbed("Success!", Color.GREEN, message);
  }

  private MessageEmbed createErrorMessageEmbed(String message) {
    return createMessageEmbed("Error!", Color.RED, message);
  }

  private static MessageEmbed createMessageEmbed(String title, Color color, String message) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(title);
    embedBuilder.setDescription(message);
    embedBuilder.setColor(color);
    return embedBuilder.build();
  }

  private DiscordResponse getListOfCommands(List<Command> commands) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle("Commands");
    for (Command com : commands) {
      embedBuilder.addField(new CommandProcessor().getPrefix() + com.getCommandUsage(), com.getDescription(), false);
    }
    return new DiscordResponse(embedBuilder.build());
  }
}
