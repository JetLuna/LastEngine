package net.jetluna.auth.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CodeManager {

    private final Map<UUID, String> pendingCodes = new HashMap<>();
    private final Random random = new Random();

    // Генерирует код (для тг или почты)
    public String generateCode(UUID uuid, String contact) {
        String code = String.format("%06d", random.nextInt(999999));
        pendingCodes.put(uuid, code);
        return code;
    }

    public boolean checkCode(UUID uuid, String inputCode) {
        if (!pendingCodes.containsKey(uuid)) return false;
        return pendingCodes.get(uuid).equals(inputCode);
    }

    public void clear(UUID uuid) {
        pendingCodes.remove(uuid);
    }
}