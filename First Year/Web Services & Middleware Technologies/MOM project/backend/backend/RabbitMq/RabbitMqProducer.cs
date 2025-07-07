using System.Text;
using RabbitMQ.Client;

namespace backend.RabbitMq
{
    public class RabbitMqProducer
    {
        private readonly RabbitMqConnection _connection;

        public RabbitMqProducer(RabbitMqConnection connection)
        {
            _connection = connection;
        }

        public async Task SendMessageAsync(string message)
        {
            var connection = await _connection.GetConnectionAsync();
            using (var channel = await connection.CreateChannelAsync())   
            {
                await channel.QueueDeclareAsync(queue: "recipe_queue",
                                     durable: true,
                                     exclusive: false,
                                     autoDelete: false,
                                     arguments: null);

                for (int i = 1; i < 10; i++)
                {
                    var messg = $"{DateTime.UtcNow}";
                    var body = Encoding.UTF8.GetBytes(messg);

                    await channel.BasicPublishAsync(exchange: "",
                                         routingKey: "recipe_queue",
                                         mandatory: true,
                                         basicProperties: new BasicProperties { Persistent = true },
                                         body: body);

                    Console.WriteLine($"Sent: {messg}");
                    await Task.Delay(2000);
                }
            }
        }
    }
}
