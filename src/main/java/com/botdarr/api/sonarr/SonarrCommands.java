package com.botdarr.api.sonarr;

import com.botdarr.api.ContentType;
import com.botdarr.commands.BaseCommand;
import com.botdarr.commands.Command;
import com.botdarr.commands.CommandProcessor;
import com.botdarr.commands.CommandResponseUtil;
import com.botdarr.commands.responses.CommandResponse;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SonarrCommands {
  public static List<Command> getCommands(SonarrApi sonarrApi) {
    return new ArrayList<Command>() {{
      add(new BaseCommand("show id add", "show id add <show-title> <show-tvdbid>",
        "Adds a show using search text and tmdb id (i.e., show id add 30 rock 484737). The easiest" +
        " way to use this command is to use \"show find new <show-title>\", then the results will contain the show add command for you") {
        @Override
        public List<CommandResponse> execute(String command) {
          int lastSpace = command.lastIndexOf(" ");
          if (lastSpace == -1) {
            throw new RuntimeException("Missing expected arguments - usage: show id add SHOW_TITLE_HERE SHOW_ID_HERE");
          }
          String searchText = command.substring(0, lastSpace);
          String id = command.substring(lastSpace + 1);
          validateShowTitle(searchText);
          validateShowId(id);
          return Collections.singletonList(sonarrApi.addWithId(searchText, id));
        }
      });
      add(new BaseCommand("show title add", "show title add <show-title>",
        "Adds a show with just a title. Since there can be multiple shows that match search criteria" +
        " we will either add the show or return all the shows that match your search.") {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.addWithTitle(command);
        }
      });
      add(new BaseCommand("show downloads", "Shows all the active shows downloading in sonarr") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return new CommandResponseUtil().addEmptyDownloadsMessage(sonarrApi.downloads(), ContentType.SHOW);
        }
      });
      add(new BaseCommand("show profiles", "Displays all the profiles available to search for shows under (i.e., show add ANY)") {
        @Override
        public boolean hasArguments() {
          return false;
        }

        @Override
        public List<CommandResponse> execute(String command) {
          return sonarrApi.getProfiles();
        }
      });
      add(new BaseCommand("show find existing", "show find existing <show-title>", "Finds a existing show using sonarr (i.e., show find existing Ahh! Real fudgecakes)") {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.lookup(command, false);
        }
      });
      add(new BaseCommand("show find new", "show find new <show-title>", "Finds a new show using sonarr (i.e., show find new Fresh Prince of Fresh air)") {
        @Override
        public List<CommandResponse> execute(String command) {
          validateShowTitle(command);
          return sonarrApi.lookup(command, true);
        }
      });
    }};
  }

  public static String getAddShowCommandStr(String title, long tvdbId) {
    return new CommandProcessor().getPrefix() + "show id add " + title + " " + tvdbId;
  }

  public static String getHelpShowCommandStr() {
    return new CommandProcessor().getPrefix() + "shows help";
  }

  private static void validateShowTitle(String movieTitle) {
    if (Strings.isEmpty(movieTitle)) {
      throw new IllegalArgumentException("Show title is missing");
    }
  }

  private static void validateShowId(String id) {
    if (Strings.isEmpty(id)) {
      throw new IllegalArgumentException("Show id is missing");
    }
    try {
      Integer.valueOf(id);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Show id is not a number");
    }
  }
}