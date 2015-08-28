package redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class JedisWrapper <K,V> {

	// this is thread safe - no need to worry
	private static JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
	
	// val is typically a json
	public void set(K key, String templateName, V val) {
		Jedis jedis = pool.getResource();
		try {
			Transaction transaction = jedis.multi();
			transaction.set("mock.server.template."+templateName+"."+key.toString(), val.toString());
			transaction.exec();
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public String get(K key) {
		Jedis jedis = pool.getResource();
		try {
			Transaction transaction = jedis.multi();
			Response<String> resp = transaction.get("mock.server.template."+key.toString());
			transaction.exec();
			return resp.get();
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public String get(K key, String templateName) {
		Jedis jedis = pool.getResource();
		try {
			Transaction transaction = jedis.multi();
			Response<String> resp = transaction.get("mock.server.template."+templateName+"."+key.toString());
			transaction.exec();
			return resp.get();
		}
		finally {
			pool.returnResource(jedis);
		}
	}
}
