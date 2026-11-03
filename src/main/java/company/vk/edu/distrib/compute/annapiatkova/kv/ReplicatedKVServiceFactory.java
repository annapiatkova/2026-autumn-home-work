package company.vk.edu.distrib.compute.annapiatkova.kv;

import company.vk.edu.distrib.compute.AbstractHttpServiceFactory;
import company.vk.edu.distrib.compute.kv.KVService;
import company.vk.edu.distrib.compute.kv.ReplicatedKVServiceTest;
import company.vk.edu.distrib.compute.kv.RemoteDaoFactoryTest;
import java.io.IOException;

@ReplicatedKVServiceTest
@RemoteDaoFactoryTest
public class ReplicatedKVServiceFactory extends AbstractHttpServiceFactory<KVService> {
    @Override
    protected ReplicatedKVService doCreate(int port) throws IOException {
        return new ReplicatedKVService(port);
    }
}
