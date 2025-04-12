package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;

public class ExecuteScriptCommand extends Command {
    public ExecuteScriptCommand() {
        super("execute_script", "считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs().size() != 1) throw new IllegalArgumentException();

        return new Response(ResponseStatus.EXECUTE_SCRIPT, requestCommand.getArgs().get(0));
    }
}
