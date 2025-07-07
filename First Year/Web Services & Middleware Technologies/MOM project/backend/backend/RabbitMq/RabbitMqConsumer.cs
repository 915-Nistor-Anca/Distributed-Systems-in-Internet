using System.Text;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

namespace backend.RabbitMq
{
    public class RabbitMqConsumer
    {
        private readonly RabbitMqConnection _connection;

        public RabbitMqConsumer(RabbitMqConnection connection)
        {
            _connection = connection;
        }

        public async Task StartConsumingAsync()
        {
            var connection = await _connection.GetConnectionAsync();
            using (var channel = await connection.CreateChannelAsync())
            {
                await channel.QueueDeclareAsync(queue: "recipe_queue", durable: true, exclusive: false, autoDelete: false, arguments: null);
                Console.WriteLine("Waiting for messages...");

                var consumer = new AsyncEventingBasicConsumer(channel);
                consumer.ReceivedAsync += async (sender, eventArgs) =>
                {
                    var body = eventArgs.Body.ToArray();
                    var message = Encoding.UTF8.GetString(body);
                    Console.WriteLine($"Received: {message}");

                    await ((AsyncEventingBasicConsumer)sender).Channel.BasicAckAsync(eventArgs.DeliveryTag, false);
                };

                await channel.BasicConsumeAsync(queue: "recipe_queue", autoAck: false, consumer: consumer);
                Console.WriteLine(" Press [enter] to exit.");
                Console.ReadLine();
            }
        }
    }
}
