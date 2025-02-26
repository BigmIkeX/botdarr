package com.botdarr.commands;

import com.botdarr.commands.responses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelpCommands {
  public static List<Command> getCommands(List<Command> radarrCommands,
                                          List<Command> sonarrCommands,
                                          List<Command> lidarrCommands) {
    return new ArrayList<Command>() {{
      add(new BaseHelpCommand("help", "") {
        @Override
        public List<CommandResponse> execute(String command) {
          return Collections.singletonList(new HelpResponse());
        }
      });
      add(new BaseHelpCommand("movies help", "") {
        @Override
        public List<CommandResponse> execute(String command) {
          return Collections.singletonList(new MoviesHelpResponse(radarrCommands));
        }
      });
      add(new BaseHelpCommand("shows help", "") {
        @Override
        public List<CommandResponse> execute(String command) {
          return Collections.singletonList(new ShowsHelpResponse(sonarrCommands));
        }
      });
      add(new BaseHelpCommand("music help", "") {
        @Override
        public List<CommandResponse> execute(String command) {
          return Collections.singletonList(new MusicHelpResponse(lidarrCommands));
        }
      });
    }};
  }

  private static abstract class BaseHelpCommand extends BaseCommand {

    public BaseHelpCommand(String commandText, String description) {
      super(commandText, description);
    }

    @Override
    public boolean hasArguments() {
      return false;
    }
  }
}
