package yeamy.restlite.permission;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

// redis.clients:jedis:4.3.1
public class RedisPermissionManager extends PermissionManager {
    private final JedisPool pool;
    private final String PREFIX_ACCOUNT = "restliet.permission::account::";
    private final String PREFIX_ROLE = "restliet.permission::role::";
    private final String PREFIX_PERMISSION = "restliet.permission::permission::";

    private final ConcurrentHashMap<String, Permission> permissions;

    public RedisPermissionManager(JedisPool pool) {
        this(pool, true);
    }

    public RedisPermissionManager(JedisPool pool, boolean cachePermission) {
        this.pool = pool;
        if (cachePermission) {
            this.permissions = null;
            return;
        }
        ConcurrentHashMap<String, Permission> map = new ConcurrentHashMap<>();
        int l = PREFIX_PERMISSION.length();
        run(redis -> {
            for (String key : redis.keys(PREFIX_PERMISSION + "*")) {
                String rule = key.substring(l);
                map.put(rule, new Permission(rule));
            }
        });
        this.permissions = map;
    }

    @Override
    public void destroy() {
        pool.close();
    }

    private interface Task {
        void run(Jedis pipeline);
    }

    private void run(Task task) {
        try (Jedis redis = pool.getResource()) {
            task.run(redis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAccount(String id, String... roles) {
        run(redis -> redis.set(PREFIX_ACCOUNT + id, String.join(",", roles)));
    }

    @Override
    public Account getAccount(String id) {
        AtomicReference<String> roles = new AtomicReference<>();
        run(redis -> roles.set(redis.get(PREFIX_ACCOUNT + id)));
        return new Account(roles.get().split(","));
    }

    @Override
    public void deleteAccount(String id) {
        run(redis -> redis.del(PREFIX_ACCOUNT + id));
    }

    @Override
    public void setRole(String id, Collection<String> permissions) {
        run(redis -> redis.set(PREFIX_ROLE + id, String.join(",", permissions)));
    }

    @Override
    public void deleteRole(String id) {
        run(redis -> redis.del(PREFIX_ROLE + id));
    }

    @Override
    public Role getRole(String id) {
        AtomicReference<String> permissions = new AtomicReference<>();
        run(redis -> permissions.set(redis.get(PREFIX_ROLE + id)));
        return new Role(permissions.get().split(","));
    }

    @Override
    public Collection<Role> getRole(String[] ids) {
        List<Role> roles = new ArrayList<>(ids.length);
        run(redis -> redis.mget(ids).forEach(p -> {
            if (p != null) roles.add(new Role(p.split(",")));
        }));
        return roles;
    }

    @Override
    public void setPermission(String rule) {
        if (this.permissions != null) {
            Permission p = this.permissions.get(rule);
            if (p == null) {
                this.permissions.put(rule, new Permission(rule));
            } else {
                p.set(rule);
            }
        }
        run(redis -> redis.set(PREFIX_PERMISSION + rule, rule));
    }

    @Override
    public void deletePermission(String rule) {
        this.permissions.remove(rule);
        run(redis -> redis.del(PREFIX_PERMISSION + rule));
    }

    @Override
    public Permission getPermission(String rule) {
        if (this.permissions != null) {
            return this.permissions.get(rule);
        }
        AtomicReference<Boolean> exists = new AtomicReference<>();
        run(redis -> exists.set(redis.exists(PREFIX_PERMISSION + rule)));
        return exists.get() ? new Permission(rule) : null;
    }

    @Override
    public Collection<Permission> getPermission(Collection<String> rules) {
        if (this.permissions != null) {
            List<Permission> list = new ArrayList<>(rules.size());
            rules.forEach(rule -> {
                Permission p = this.permissions.get(rule);
                if (p != null) {
                    list.add(p);
                }
            });
            return list;
        }
        String[] keys = new String[rules.size()];
        int i = 0;
        for (String rule : rules) {
            keys[i++] = PREFIX_PERMISSION + rule;
        }
        List<Permission> list = new ArrayList<>(keys.length);
        run(redis -> redis.mget(keys).forEach(rule -> list.add(new Permission(rule))));
        return list;
    }

}
