using RabbitMQ.Client;

namespace backend.RabbitMq
{
    public class RabbitMqConnection
    {
        public async Task<IConnection> GetConnectionAsync()
        {
            var factory = new ConnectionFactory()
            {
                HostName = "localhost", 
                Port = 5672,
                UserName = "guest",  
                Password = "guest",
            };

            return await factory.CreateConnectionAsync();
        }
    }
}
