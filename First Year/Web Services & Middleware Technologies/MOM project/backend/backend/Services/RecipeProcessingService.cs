using backend.RabbitMq;
using Microsoft.Extensions.Hosting;

namespace backend.Services
{
    public class RecipeProcessingService : BackgroundService
    {
        private readonly RabbitMqConsumer _rabbitMqConsumer;

        public RecipeProcessingService(RabbitMqConsumer rabbitMqConsumer)
        {
            _rabbitMqConsumer = rabbitMqConsumer;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                await _rabbitMqConsumer.StartConsumingAsync();
                await Task.Delay(1000, stoppingToken);
            }
        }
    }
}
