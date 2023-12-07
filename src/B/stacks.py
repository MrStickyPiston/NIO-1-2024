class Box:
	def __init__(self, width, length, height):
		self.width = width
		self.length = length
		self.height = height

	def __str__(self):
		return f'<w{self.width} l{self.length} h{self.height}>'

	def __repr__(self):
		return self.__str__()

	def fits_on(self, box):
		return box.width > self.width and box.length > self.length


def stack_boxes(stack: list[Box]):
	box_fits_on = False

	for b in boxes:
		if b.fits_on(stack[-1]):
			stack_boxes(stack + [b])
			box_fits_on = True

	if not box_fits_on:
		result.update({get_height(stack): stack})


def get_height(stack: list[Box]):
	height = 0

	for b in stack:
		height += b.height

	return height


boxes: list[Box] = []
result: dict[int:Box] = {}


def main(raw_boxes: list[list[int]]):
	raw_boxes.sort(reverse=True)

	for rb in raw_boxes:
		rb.sort(reverse=True)
		boxes.append(Box(rb[0], rb[1], rb[2]))
		boxes.append(Box(rb[0], rb[2], rb[1]))
		boxes.append(Box(rb[1], rb[2], rb[0]))

	for b in boxes:
		stack_boxes([b])

	sorted_result = dict(sorted(result.items(), reverse=True))
	print(sorted_result)
	print(max(result))


if __name__ == '__main__':
	_boxes = [
		[5, 15, 27],
		[14, 18, 26],
		[14, 20, 31],
		[14, 24, 38],
		[15, 27, 33],
		[2, 9, 20]
	]
	main(_boxes)
