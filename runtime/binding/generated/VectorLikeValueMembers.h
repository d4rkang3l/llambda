/*****************************************************************
 * This file is generated by gen-types.py. Do not edit manually. *
 *****************************************************************/

public:
	std::uint32_t length() const
	{
		return m_length;
	}

	BoxedValue** elements() const
	{
		return m_elements;
	}

private:
	std::uint32_t m_length;
	BoxedValue** m_elements;