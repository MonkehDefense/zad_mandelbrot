using System.Text;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System.Threading.Tasks;
using System.Threading;





namespace Receive{
	class Receive{
		static void Main(string[] args)
		{
			String address = "wklep tu ip, gościu";


			var factory = new ConnectionFactory()
			{
				HostName = address,
				Password = "wdprir",
				UserName = "wdprir",
				VirtualHost = "wdprir"
			};


			
			using (var connection = factory.CreateConnection())
			using (var channel = connection.CreateModel())
			{


				channel.ExchangeDeclare(
					exchange: "wdprir_fanout",
					type: "fanout"
				);

				var kolejek = channel.QueueDeclare(queue: "",
					durable: false,
					exclusive: true,
					autoDelete: false,
					arguments: null
				);

				channel.QueueBind(
					queue: kolejek.QueueName,
					exchange: "wdprir_fanout",
					routingKey: ""
				);






				



				var consumer = new EventingBasicConsumer(channel);
				consumer.Received += (ModuleHandle, ea) =>
				{
					var body = ea.Body.ToArray();
					var message = Encoding.UTF8.GetString(body);
					Console.WriteLine(" [x]: {0}", message);
				};

				channel.BasicConsume(
					queue: kolejek.QueueName,
					autoAck: true,
					consumer: consumer
				);






				Task task_recieve = Task.Factory.StartNew(() => {
					Console.WriteLine("Czat otwarty.");

					while(true){
						var message = Console.ReadLine();
						if(String.IsNullOrEmpty(message)){
							break;
						}
						var body = Encoding.UTF8.GetBytes(message);

						channel.BasicPublish(
							exchange: "wdprir_fanout",
							routingKey: "",
							basicProperties: null,
							body: body
						);
					}
				});


				task_recieve.Wait();

			}
		}


	}
}
