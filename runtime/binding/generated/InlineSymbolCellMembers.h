/************************************************************
 * This file is generated by typegen. Do not edit manually. *
 ************************************************************/

public:
	std::uint8_t inlineCharLength() const
	{
		return m_inlineCharLength;
	}

	const std::uint8_t* inlineData() const
	{
		return m_inlineData;
	}

	std::uint8_t* inlineData()
	{
		return m_inlineData;
	}

private:
	std::uint8_t m_inlineCharLength;
	std::uint8_t m_inlineData[28];
